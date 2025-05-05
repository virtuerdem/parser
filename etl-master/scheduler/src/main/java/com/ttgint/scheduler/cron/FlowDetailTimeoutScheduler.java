package com.ttgint.scheduler.cron;

import com.ttgint.library.enums.FlowStatus;
import com.ttgint.library.model.FlowDetail;
import com.ttgint.library.repository.FlowDetailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowDetailTimeoutScheduler {

    private final FlowDetailRepository flowDetailRepository;

    @Scheduled(cron = "*/20 * * * * *")
    public void killTimeoutProcesses() {

        List<FlowDetail> candidates = flowDetailRepository
                .findAllByEngineStatusIn(List.of(FlowStatus.RUNNING))
                .stream()
                .filter(fd -> (fd.getLastExecuteTime() != null || fd.getTimeoutThresholdMin() != null) && fd.getLastExecuteTime().isBefore(OffsetDateTime.now().minusMinutes(fd.getTimeoutThresholdMin())))
                .filter(fd -> fd.getLastExecutePid() != null && fd.getLastExecutePid().matches("\\d+"))
                .filter(fd -> fd.getIsContinueAfterTimout() != null && fd.getIsContinueAfterTimout())
                .toList();

        for (FlowDetail detail : candidates) {
            String pid = detail.getLastExecutePid();
            try {
                Process process = new ProcessBuilder("kill", "-9", pid).start();
                process.waitFor();
                log.info("FlowDetail process killed PID: {}, FlowDetailId: {}", pid, detail.getId());

                flowDetailRepository.updateStatusById(detail.getId(), FlowStatus.STOPPED);
            } catch (Exception e) {
                log.error("Failed to kill process with PID: {}", pid, e);
            }
        }
    }
}
