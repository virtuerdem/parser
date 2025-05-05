package com.ttgint.transfer.operation.engine;

import com.ttgint.library.record.DecompressRecord;
import com.ttgint.library.repository.NetworkItemRepository;
import com.ttgint.transfer.base.TransferBaseEngine;
import com.ttgint.transfer.operation.handler.HwNbPmCsvTransferHandler;
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
@Component("HW_NB_PM_CSV_TRANSFER")
public class HwNbPmCsvTransferEngine extends TransferBaseEngine {
    private final NetworkItemRepository networkItemRepository;

    public HwNbPmCsvTransferEngine(ApplicationContext applicationContext, NetworkItemRepository networkItemRepository) {
        super(applicationContext);
        this.networkItemRepository = networkItemRepository;
    }

    @Override
    protected void onEngine() {
        log.info("* HwNbPmCsvTransferEngine onTransfer");
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
                                new HwNbPmCsvTransferHandler(
                                        applicationContext,
                                        getTransferHandlerRecord(connection),
                                        items)
                        );
                    } catch (Exception exception) {
                        log.error("! HwNbPmCsvTransferEngine onProcess connectionId:{} error: {}", connection.getId(),
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
    protected DecompressRecord getDecompressRecord(File file) {
        return DecompressRecord.getRecord(engineRecord,
                file,
                OffsetDateTime.parse(
                        file.getName()
                                .split("_")[file.getName().split("_").length - 2] + " +03:00",
                        DateTimeFormatter.ofPattern("yyyyMMddHHmm XXX")),
                (file.getName().contains("^^") ? file.getName().split("\\^")[0] : null),
                null,
                file.getName());
    }
}
