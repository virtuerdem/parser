package com.ttgint.transfer.operation.engine;

import com.ttgint.library.record.DecompressRecord;
import com.ttgint.library.repository.NetworkItemRepository;
import com.ttgint.transfer.base.TransferBaseEngine;
import com.ttgint.transfer.operation.handler.HwAbidCmTransferHandler;
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
@Component("HW_ABID_CM_TRANSFER")
public class HwAbidCmTransferEngine extends TransferBaseEngine {
    private final NetworkItemRepository networkItemRepository;

    public HwAbidCmTransferEngine(ApplicationContext applicationContext) {
        super(applicationContext);
        this.networkItemRepository = applicationContext.getBean(NetworkItemRepository.class);
    }

    @Override
    protected void onEngine() {
        log.info("* HwAbidCmTransferEngine onTransfer");
        List<String> items
                = networkItemRepository.findActiveItemCodesByFlowId(engineRecord.getFlowId());

        ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getOnTransferThreadCount());
        getConnections()
                .forEach(connection -> {
                    try {
                        executor.execute(
                                new HwAbidCmTransferHandler(
                                        applicationContext,
                                        getTransferHandlerRecord(connection),
                                        items)
                        );
                    } catch (Exception exception) {
                        log.error("! HwAbidCmTransferEngine onProcess connectionId:{} error: {}", connection.getId(),
                                exception.getMessage());
                    }
                });
        shutdownExecutorService(executor);
    }


    @Override
    protected ArrayList<File> getDecompressFiles() {
        return new ArrayList<>(fileLib.readFilesInCurrentPathByContains(engineRecord.getRawPath(), ".txt"));
    }

    @Override
    protected DecompressRecord getDecompressRecord(File file) {
        return DecompressRecord.getRecord(engineRecord,
                file,
                OffsetDateTime.parse(
                        file.getName()
                                .split("-", 2)[1]
                                .substring(0, 10) + "00+03:00",
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mmXXX")),
                (file.getName().contains("^^") ? file.getName().split("\\^")[0] : null),
                null,
                file.getName());
    }
}
