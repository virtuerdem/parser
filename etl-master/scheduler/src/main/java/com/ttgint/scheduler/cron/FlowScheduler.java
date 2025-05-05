package com.ttgint.scheduler.cron;

import com.ttgint.library.enums.FlowStatus;
import com.ttgint.library.model.Flow;
import com.ttgint.library.model.FlowDetail;
import com.ttgint.library.model.FlowProcessHistory;
import com.ttgint.library.repository.FlowDetailRepository;
import com.ttgint.library.repository.FlowRepository;
import com.ttgint.library.repository.ManagerRepository;
import com.ttgint.scheduler.utils.EtlTimeUtils;
import com.ttgint.scheduler.utils.HistoryUtils;
import com.ttgint.scheduler.utils.LogListenerUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowScheduler {

    @Value("${app.scheduler.managerCode}")
    private String managerCode;
    @Value("${app.scheduler.logPath}")
    private String logPath;

    @Value("${app.scheduler.path.java}")
    private String javaPath;
    @Value("${app.scheduler.path.transfer-engine}")
    private String transferEnginePath;
    @Value("${app.scheduler.path.parse-engine}")
    private String parseEnginePath;
    @Value("${app.scheduler.path.loader-engine}")
    private String loaderEnginePath;

    private final FlowRepository flowRepository;
    private final FlowDetailRepository flowDetailRepository;
    private final ManagerRepository managerRepository;
    private final HistoryUtils historyUtils;

    @Scheduled(cron = "*/20 * * * * *")
    public void run() {
        try {
            long managerId = managerRepository.findByManagerCode(managerCode).getId();
            OffsetDateTime triggerTime = EtlTimeUtils.getTriggerTime();

            log.info("*Scheduler Check for Flows: {}", triggerTime);

            flowRepository.findAllByIsActive(true)
                    .stream()
                    .filter(f -> Objects.equals(managerId, f.getActiveManagerId())
                                    && (f.getFlowStatus().equals(FlowStatus.FORCED) ||
                                    (f.getFlowStatus().equals(FlowStatus.STOPPED)
                                            && EtlTimeUtils.cronCheck(f.getFlowCron(), triggerTime)
                                    )
                            )
                    )
                    .sorted(Comparator.comparing(Flow::getId))
                    .forEach(flow -> Thread.ofVirtual().name(flow.getFlowCode()).start(() -> executeFlow(flow)));
        } catch (Exception e) {
            log.error("*Scheduler Check Error for Flows ", e);
        }
        log.info("All jobs completed.");
    }

    private void executeFlow(Flow flow) {
        log.info("FlowId: {} started!", flow.getId());

        FlowStatus flowStatus = flow.getFlowStatus();
        flowRepository.updateFlowStatusById(flow.getId(), FlowStatus.TRIGGERED);
        flowDetailRepository.updateStatusByFlowId(flow.getId(), flowStatus, FlowStatus.TRIGGERED);

        String flowProcessCode
                = OffsetDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss")) +
                String.format("%1$" + 6 + "s", flow.getId()).replace(' ', '0');
        FlowProcessHistory history = historyUtils.startFlowHistory(flow.getId(), flowProcessCode);

        try {
            List<FlowDetail> detailList
                    = flowDetailRepository.findAllByFlowIdAndEngineStatus(flow.getId(), FlowStatus.TRIGGERED)
                    .stream()
                    .sorted(Comparator.comparing(FlowDetail::getExecuteOrder))
                    .toList();
            for (FlowDetail detail : detailList) {
                if (!executeFlowDetail(detail, flowProcessCode)) {
                    log.info("Error executing command: {}", detail);
                    // todo alarm oluşturulacak..
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        historyUtils.endFlowHistory(history);
        flowRepository.updateFlowStatusById(flow.getId(), FlowStatus.STOPPED);
        log.info("FlowId: {} completed!", flow.getId());
    }

    private boolean executeFlowDetail(FlowDetail flowDetail, String flowProcessCode) {
        try {
            String logDirectoryPath = createLogDirectory(flowDetail.getFlowId());
            String infoLogPath = String.format("%s/%s_%s_info.log",
                    logDirectoryPath, flowProcessCode, flowDetail.getEngineType().getValue());
            String errorLogPath = String.format("%s/%s_%s_error.log",
                    logDirectoryPath, flowProcessCode, flowDetail.getEngineType().getValue());

            Process process = new ProcessBuilder(getCmd(flowDetail, flowProcessCode).split(" +"))
                    .redirectErrorStream(false)
                    .start();

            flowDetail.setLastExecuteTime(OffsetDateTime.now());
            flowDetail.setLastExecutePid(process.pid() + "");
            flowDetail.setEngineStatus(FlowStatus.RUNNING);
            flowDetailRepository.save(flowDetail);

            ExecutorService executor = Executors.newFixedThreadPool(2);
            executor.execute(new LogListenerUtils(process.getInputStream(), System.out::println, infoLogPath));
            executor.execute(new LogListenerUtils(process.getErrorStream(), System.err::println, errorLogPath));
            executor.shutdown();

            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow(); // Görevler takılırsa terminate edilsin
            }

            int exitCode = process.waitFor();
            log.info("FlowDetail {} finished with exit code: {}", flowDetail.getId(), exitCode);

            return exitCode == 0;
        } catch (Exception e) {
            log.error("Exception during command execution for FlowDetail {}: {}", flowDetail.getId(), e.getMessage(), e);
            return false;
        } finally {
            flowDetailRepository.updateStatusById(flowDetail.getId(), FlowStatus.STOPPED);
        }
    }

    private String getCmd(FlowDetail flowDetail, String flowCode) {
        String enginePath = switch (flowDetail.getEngineType()) {
            case TRANSFER -> transferEnginePath;
            case PARSE -> parseEnginePath;
            case LOADER -> loaderEnginePath;
            default -> throw new IllegalStateException("Unexpected EngineType: " + flowDetail.getEngineType());
        };

        return String.format("%s %s -jar %s %s %s",
                javaPath,
                (flowDetail.getIsJvmParam() ? flowDetail.getJvmParam() : ""),
                enginePath,
                flowDetail.getFlowId(),
                flowCode
        );
    }

    private String createLogDirectory(Long flowId) throws IOException {
        // logPath/yyyy/MM/dd/flowId/
        OffsetDateTime today = OffsetDateTime.now();
        String logFileName = String.format("%s/%s/%s/%s/%s",
                logPath,
                today.getYear(),
                String.format("%02d", today.getMonthValue()),
                String.format("%02d", today.getDayOfMonth()),
                flowId).replace("//", "/");

        Files.createDirectories(Paths.get(logFileName));
        return logFileName;
    }
}
