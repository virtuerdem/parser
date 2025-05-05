package com.ttgint.transfer.operation.engine;

import com.ttgint.library.record.DecompressRecord;
import com.ttgint.library.repository.NetworkItemRepository;
import com.ttgint.transfer.base.TransferBaseEngine;
import com.ttgint.transfer.operation.handler.HwLogsNeConfTransferHandler;
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
@Component("HW_LOGS_NE_CONF_TRANSFER")
public class HwLogsNeConfTransferEngine extends TransferBaseEngine {

    private final NetworkItemRepository networkItemRepository;

    public HwLogsNeConfTransferEngine(ApplicationContext applicationContext) {
        super(applicationContext);
        this.networkItemRepository = applicationContext.getBean(NetworkItemRepository.class);
    }

    @Override
    protected void onEngine() {
        log.info("* HwLogsNeConfTransferEngine onTransfer");
        List<String> items
                = networkItemRepository.findActiveItemCodesByFlowId(engineRecord.getFlowId());

        ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getOnTransferThreadCount());
        getConnections()
                .forEach(connection -> {
                    try {
                        executor.execute(
                                new HwLogsNeConfTransferHandler(
                                        applicationContext,
                                        getTransferHandlerRecord(connection),
                                        items)
                        );
                    } catch (Exception exception) {
                        log.error("! HwLogsNeConfTransferEngine onProcess connectionId:{} error: {}", connection.getId(),
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
                                .split("_")[file.getName().split("_").length - 2]
                                .substring(0, 8) + " 00:00+03:00",
                        DateTimeFormatter.ofPattern("yyyyMMdd HH:mmXXX")),
                (file.getName().contains("^^") ? file.getName().split("\\^")[0] : null),
                null,
                file.getName());
    }
}
