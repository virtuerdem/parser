package com.ttgint.transfer.operation.engine;

import com.ttgint.library.record.DecompressRecord;
import com.ttgint.library.repository.NetworkItemRepository;
import com.ttgint.transfer.base.TransferBaseEngine;
import com.ttgint.transfer.operation.handler.DfCliPmTransferHandler;
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
@Component("DF_CLI_PM_TRANSFER")
public class DfCliPmTransferEngine extends TransferBaseEngine {

    private final NetworkItemRepository networkItemRepository;

    public DfCliPmTransferEngine(ApplicationContext applicationContext) {
        super(applicationContext);
        this.networkItemRepository = applicationContext.getBean(NetworkItemRepository.class);
    }

    @Override
    protected void onEngine() {
        log.info("* DfCliPmTransferEngine onTransfer");
        List<String> items
                = networkItemRepository.findActiveItemCodesByFlowId(engineRecord.getFlowId());

        ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getOnTransferThreadCount());
        getConnections()
                .forEach(connection -> {
                    try {
                        executor.execute(
                                new DfCliPmTransferHandler(
                                        applicationContext,
                                        getTransferHandlerRecord(connection),
                                        items)
                        );
                    } catch (Exception exception) {
                        log.error("! DfCliPmTransferEngine onProcess connectionId:{} error: {}", connection.getId(),
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
                getDate(file.getName()),
                (file.getName().contains("^^") ? file.getName().split("\\^")[0] : null),
                null,
                file.getName());
    }

    public OffsetDateTime getDate(String fileName) {
        OffsetDateTime date = OffsetDateTime.parse(
                fileName
                        .split(".csv.")[1]
                        .substring(0, 13) + ":00+03:00",
                DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mmXXX"));
        if (fileName.endsWith("-12-1.csv")) {
            date = date.minusHours(12);
        } else if (!fileName.endsWith("-12-2.csv") && fileName.endsWith("-2.csv")) {
            date = date.plusHours(12);
        }
        return date;
    }
}
