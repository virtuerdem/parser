package com.ttgint.transfer.operation.engine;

import com.ttgint.library.repository.NetworkNodeRepository;
import com.ttgint.transfer.base.TransferBaseEngine;
import com.ttgint.transfer.operation.handler.UlEnbPmTransferHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component("UL_ENB_PM_TRANSFER")
public class UlEnbPmTransferEngine extends TransferBaseEngine {

    private final NetworkNodeRepository networkNodeRepository;

    public UlEnbPmTransferEngine(ApplicationContext applicationContext) {
        super(applicationContext);
        this.networkNodeRepository = applicationContext.getBean(NetworkNodeRepository.class);
    }

    @Override
    protected void onEngine() {
        log.info("* UlEnbPmTransferHandler on Transfer");
        List<String> nodes
                = networkNodeRepository.findActiveNodeNamesByFlowId(engineRecord.getFlowId());

        ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getOnTransferThreadCount());
        getConnections()
                .forEach(connection -> {
                    try {
                        executor.execute(
                                new UlEnbPmTransferHandler(
                                        applicationContext,
                                        getTransferHandlerRecord(connection),
                                        nodes)
                        );
                    } catch (Exception exception) {
                        log.error("! UlEnbPmTransferHandler onProcess connectionId:{} error: {}", connection.getId(),
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
    protected OffsetDateTime getDecompressRecordTime(String fileName) {
        return getDecompressRecordTime(
                fileName
                        .split("C")[1].substring(0, 18),
                "yyyyMMdd.HHmmZ");
    }
}