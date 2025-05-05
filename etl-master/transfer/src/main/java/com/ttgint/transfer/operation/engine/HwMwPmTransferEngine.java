package com.ttgint.transfer.operation.engine;

import com.ttgint.library.record.DecompressRecord;
import com.ttgint.library.repository.NetworkItemRepository;
import com.ttgint.transfer.base.TransferBaseEngine;
import com.ttgint.transfer.operation.handler.HwMwPmTransferHandler;
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
@Component("HW_MW_PM_TRANSFER")
public class HwMwPmTransferEngine extends TransferBaseEngine {

    private final NetworkItemRepository networkItemRepository;

    public HwMwPmTransferEngine(ApplicationContext applicationContext) {
        super(applicationContext);
        this.networkItemRepository = applicationContext.getBean(NetworkItemRepository.class);
    }

    @Override
    protected void onEngine() {
        log.info("* HwMwPmTransferEngine onTransfer");
        List<String> items
                = networkItemRepository.findActiveItemCodesByFlowId(engineRecord.getFlowId());

        ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getOnTransferThreadCount());
        getConnections()
                .forEach(connection -> {
                    try {
                        executor.execute(
                                new HwMwPmTransferHandler(
                                        applicationContext,
                                        getTransferHandlerRecord(connection),
                                        items)
                        );
                    } catch (Exception exception) {
                        log.error("! HwMwPmTransferEngine onProcess connectionId:{} error: {}", connection.getId(),
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
        String fileDate = file.getName().split("_")[file.getName().split("_").length - 2] + " 00:00+03:00";
        return DecompressRecord.getRecord(engineRecord,
                file,
                OffsetDateTime.parse(
                        fileDate,
                        DateTimeFormatter.ofPattern(
                                (fileDate.split("-")[0].length() == 4
                                        ? "yyyy-MM-dd" : "MM-dd-yyyy") + " HH:mmXXX")),
                (file.getName().contains("^^") ? file.getName().split("\\^")[0] : null),
                null,
                file.getName());
    }
}
