package com.ttgint.transfer.operation.engine;

import com.ttgint.library.repository.NetworkItemRepository;
import com.ttgint.transfer.base.TransferBaseEngine;
import com.ttgint.transfer.operation.handler.HwNbCsvPmTransferHandler;
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
@Component("HW_NB_CSV_PM_TRANSFER")
public class HwNbCsvPmTransferEngine extends TransferBaseEngine {
    private final NetworkItemRepository networkItemRepository;

    public HwNbCsvPmTransferEngine(ApplicationContext applicationContext, NetworkItemRepository networkItemRepository) {
        super(applicationContext);
        this.networkItemRepository = networkItemRepository;
    }

    @Override
    protected void onEngine() {
        log.info("* HwNbCsvPmTransferEngine onTransfer");
        List<String> items
                = networkItemRepository.findByFlowIdAndIsActive(engineRecord.getFlowId(), true)
                .stream()
                .map(e -> "pmresult_" + e.getItemCode() + "_" + e.getSourceTimePeriod() + "_")
                .toList();

        ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getOnTransferThreadCount());
        getConnections()
                .forEach(connection -> {
                    try {
                        executor.execute(
                                new HwNbCsvPmTransferHandler(
                                        applicationContext,
                                        getTransferHandlerRecord(connection),
                                        items)
                        );
                    } catch (Exception exception) {
                        log.error("! HwNbCsvPmTransferEngine onProcess connectionId:{} error: {}", connection.getId(),
                                exception.getMessage());
                    }
                });
        shutdownExecutorService(executor);
    }

    @Override
    protected ArrayList<File> getDecompressFiles() {
        return new ArrayList<>(fileLib.readFilesInCurrentPathByContains(engineRecord.getRawPath(), ".csv"));
    }


    @Override
    protected OffsetDateTime getDecompressRecordTime(String fileName) {
        return getDecompressRecordTime(
                fileName
                        .split("_")[fileName.split("_").length - 2] + " +03:00",
                "yyyyMMddHHmm XXX");
    }
}
