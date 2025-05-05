package com.ttgint.transfer.operation.engine;

import com.ttgint.library.model.GemsCmNode;
import com.ttgint.library.record.DecompressRecord;
import com.ttgint.library.repository.GemsCmNodeRepository;
import com.ttgint.transfer.base.TransferBaseEngine;
import com.ttgint.transfer.operation.handler.IgCorePmTransferHandler;
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
@Component("IG_CORE_PM_TRANSFER")
public class IgCorePmTransferEngine extends TransferBaseEngine {

    private final GemsCmNodeRepository gemsCmNodeRepository;

    public IgCorePmTransferEngine(ApplicationContext applicationContext) {
        super(applicationContext);
        this.gemsCmNodeRepository = applicationContext.getBean(GemsCmNodeRepository.class);
    }

    @Override
    protected void onEngine() {
        log.info("* IgPmCoreTransferEngine onTransfer");
        List<String> nodes
                = gemsCmNodeRepository.findAll().stream().map(GemsCmNode::getNodeName).toList();

        ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getOnTransferThreadCount());
        getConnections()
                .forEach(connection -> {
                    try {
                        executor.execute(
                                new IgCorePmTransferHandler(
                                        applicationContext,
                                        getTransferHandlerRecord(connection),
                                        nodes)
                        );
                    } catch (Exception exception) {
                        log.error("! IgPmCoreTransferEngine onProcess connectionId:{} error: {}", connection.getId(),
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
                                .split("A")[1]
                                .substring(0, 18),
                        DateTimeFormatter.ofPattern("yyyyMMdd.HHmmZ")),
                (file.getName().contains("^^") ? file.getName().split("\\^")[0] : null),
                null,
                file.getName());
    }

}
