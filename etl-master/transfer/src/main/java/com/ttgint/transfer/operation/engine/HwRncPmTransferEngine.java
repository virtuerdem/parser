package com.ttgint.transfer.operation.engine;

import com.ttgint.library.record.DecompressRecord;
import com.ttgint.library.repository.NetworkNodeRepository;
import com.ttgint.transfer.base.TransferBaseEngine;
import com.ttgint.transfer.operation.handler.HwRncPmTransferHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component("HW_RNC_PM_TRANSFER")
public class HwRncPmTransferEngine extends TransferBaseEngine {

    private final NetworkNodeRepository networkNodeRepository;

    public HwRncPmTransferEngine(ApplicationContext applicationContext) {
        super(applicationContext);
        this.networkNodeRepository = applicationContext.getBean(NetworkNodeRepository.class);
    }

    @Override
    protected void onEngine() {
        log.info("* HwRncPmTransferEngine onTransfer");
        List<String> nodes
                = networkNodeRepository.findActiveNodeNamesByBranchId(engineRecord.getBranchId());

        ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getOnTransferThreadCount());
        getConnections()
                .forEach(connection -> {
                    try {
                        executor.execute(
                                new HwRncPmTransferHandler(
                                        applicationContext,
                                        getTransferHandlerRecord(connection),
                                        nodes)
                        );
                    } catch (Exception exception) {
                        log.error("! HwRncPmTransferEngine onProcess connectionId:{} error: {}", connection.getId(),
                                exception.getMessage());
                    }
                });
        shutdownExecutorService(executor);
    }

    @Override
    protected ArrayList<File> getDecompressFiles() {
        return new ArrayList<>(fileLib.readFilesInCurrentPathByContains(engineRecord.getRawPath(), ".xml"));
    }

    @Override
    protected DecompressRecord getDecompressRecord(File file) {
        return DecompressRecord.getRecord(engineRecord,
                file,
                OffsetDateTime.parse(
                        file.getName()
                                .split("A")[1].substring(0, 18),
                        DateTimeFormatter.ofPattern("yyyyMMdd.HHmmZ")),
                (file.getName().contains("^^") ? file.getName().split("\\^")[0] : null),
                null,
                file.getName());
    }
}
