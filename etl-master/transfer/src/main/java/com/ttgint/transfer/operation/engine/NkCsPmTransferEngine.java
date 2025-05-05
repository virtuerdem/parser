package com.ttgint.transfer.operation.engine;

import com.ttgint.library.record.DecompressRecord;
import com.ttgint.library.repository.NetworkItemRepository;
import com.ttgint.transfer.base.TransferBaseEngine;
import com.ttgint.transfer.operation.handler.NkCsPmTransferHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component("NK_CS_PM_TRANSFER")
public class NkCsPmTransferEngine extends TransferBaseEngine {

    private final NetworkItemRepository networkItemRepository;

    public NkCsPmTransferEngine(ApplicationContext applicationContext) {
        super(applicationContext);
        this.networkItemRepository = applicationContext.getBean(NetworkItemRepository.class);
    }

    @Override
    protected void onEngine() {
        log.info("* NkCsPmTransferEngine onTransfer");
        List<String> items = networkItemRepository.findActiveItemCodesByFlowId(engineRecord.getFlowId());

        ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getOnTransferThreadCount());
        getConnections()
                .forEach(connection -> {
                    try {
                        executor.execute(
                                new NkCsPmTransferHandler(
                                        applicationContext,
                                        getTransferHandlerRecord(connection),
                                        items)
                        );
                    } catch (Exception exception) {
                        log.error("! NkCsPmTransferEngine onProcess connectionId:{} error: {}", connection.getId(),
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
                                .split("PM")[1]
                                .substring(0, 17),
                        DateTimeFormatter.ofPattern("yyyyMMddHHmmZ")
                ).truncatedTo(ChronoUnit.HOURS),
                (file.getName().contains("^^") ? file.getName().split("\\^")[0] : null),
                null,
                file.getName());
    }
}
