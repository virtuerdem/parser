package com.ttgint.transfer.operation.engine;

import com.ttgint.library.record.DecompressRecord;
import com.ttgint.library.repository.NetworkItemRepository;
import com.ttgint.library.repository.NetworkNodeRepository;
import com.ttgint.transfer.base.TransferBaseEngine;
import com.ttgint.transfer.operation.handler.OthHpeCmTransferHandler;
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
@Component("OTH_HPE_CM_TRANSFER")
public class OthHpeCmTransferEngine extends TransferBaseEngine {

    private final NetworkItemRepository networkItemRepository;
    private final NetworkNodeRepository networkNodeRepository;

    public OthHpeCmTransferEngine(ApplicationContext applicationContext) {
        super(applicationContext);
        this.networkItemRepository = applicationContext.getBean(NetworkItemRepository.class);
        this.networkNodeRepository = applicationContext.getBean(NetworkNodeRepository.class);
    }

    @Override
    protected void onEngine() {
        log.info("* HpHpCmTransferEngine onTransfer");
        List<String> items
                = networkItemRepository.findActiveItemCodesByFlowId(engineRecord.getFlowId());

        List<String> nodes
                = networkNodeRepository.findActiveNodeNamesByFlowId(engineRecord.getFlowId());

        ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getOnTransferThreadCount());
        getConnections()
                .forEach(connection -> {
                    try {
                        executor.execute(
                                new OthHpeCmTransferHandler(
                                        applicationContext,
                                        getTransferHandlerRecord(connection),
                                        items,
                                        nodes)
                        );
                    } catch (Exception exception) {
                        log.error("! HpHpCmTransferEngine onProcess connectionId:{} error: {}", connection.getId(),
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
                        file.getName().split("_", 3)[1].substring(0, file.getName().split("_", 3)[1].length() - 2) + " +03:00",
                        DateTimeFormatter.ofPattern("yyyyMMddHHmm XXX")),
                (file.getName().contains("^^") ? file.getName().split("\\^")[0] : null),
                null,
                file.getName());
    }
}
