package com.ttgint.transfer.base;

import com.ttgint.library.decompress.DecompressFactory;
import com.ttgint.library.model.Connection;
import com.ttgint.library.record.DecompressRecord;
import com.ttgint.library.record.TransferEngineRecord;
import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.library.repository.ConnectionRepository;
import com.ttgint.library.util.FileLib;
import com.ttgint.library.validation.XmlValidation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class TransferBaseEngine {

    protected final ApplicationContext applicationContext;
    protected final ConnectionRepository connectionRepository;
    protected final FileLib fileLib;

    protected TransferEngineRecord engineRecord;

    public TransferBaseEngine(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.connectionRepository = applicationContext.getBean(ConnectionRepository.class);
        this.fileLib = applicationContext.getBean(FileLib.class);
    }

    public void startEngine(TransferEngineRecord record) {
        log.info("* TransferBaseEngine startEngine");
        this.engineRecord = record;
        preparePaths();
        if (record.getPreTransfer()) {
            preEngine();
        }
        if (record.getOnTransfer()) {
            onEngine();
        }
        if (record.getDecompress()) {
            decompress();
        }
        if (record.getValidation()) {
            validation();
        }
        if (record.getPostTransfer()) {
            postEngine();
        }
    }

    private void preparePaths() {
        log.info("* TransferBaseEngine preparePaths");
        fileLib.createFlowPaths(engineRecord.getFlowCode());
        engineRecord.setRawPath(fileLib.createRawPath(engineRecord.getFlowCode()));
    }

    protected void preEngine() {
        log.info("* TransferBaseEngine preEngine");
    }

    protected List<Connection> getConnections() {
        List<Connection> connections = new ArrayList<>(
                connectionRepository.findByFlowIdAndIsActive(engineRecord.getFlowId(), true));
        log.info("* TransferBaseEngine getConnections connectionSize: {}", connections.size());
        return connections;
    }

    protected TransferBaseHandler getTransferHandler(Connection connection) {
        return null;
    }

    protected TransferHandlerRecord getTransferHandlerRecord(Connection connection) {
        return TransferHandlerRecord.getRecord(engineRecord, connection);
    }

    protected void onEngine() {
        log.info("* TransferBaseEngine onEngine");
        ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getOnTransferThreadCount());
        getConnections()
                .forEach(connection -> {
                    try {
                        executor.execute(getTransferHandler(connection));
                    } catch (Exception exception) {
                        log.error("! TransferBaseEngine onProcess connectionId:{} error: {}", connection.getId(),
                                exception.getMessage());
                    }
                });
        shutdownExecutorService(executor);
    }

    protected ArrayList<File> getDecompressFiles() {
        return null;
    }

    protected DecompressRecord getDecompressRecord(File file) {
        return null;
    }

    protected void decompress() {
        log.info("* TransferBaseEngine decompress");
        ArrayList<File> files = getDecompressFiles();
        if (files != null) {
            log.info("* TransferBaseEngine decompress fileSize: {}", files.size());
            ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getDecompressThreadCount());
            files.forEach(file -> {
                try {
                    executor.execute(
                            new DecompressFactory(applicationContext, getDecompressRecord(file))
                                    .getDecompress()
                    );
                } catch (Exception exception) {
                    log.error("! TransferBaseEngine onProcess fileName:{} error: {}", file.getName(), exception.getMessage());
                }
            });
            shutdownExecutorService(executor);
        } else {
            log.info("* TransferBaseEngine decompress fileSize: null");
        }
    }

    protected void validation() {
        log.info("* TransferBaseEngine validation");
        ArrayList<File> files
                = new ArrayList<>(fileLib.readFilesInWalkingPathByPostfix(engineRecord.getRawPath(), ".xml"));
        if (!files.isEmpty()) {
            log.info("* TransferBaseEngine validation fileSize: {}", files.size());
            ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getValidationThreadCount());
            files.forEach(file -> {
                try {
                    executor.execute(new XmlValidation(file.getAbsolutePath()));
                } catch (Exception exception) {
                    log.error("! TransferBaseEngine validation fileName:{} error: {}", file.getName(), exception.getMessage());
                }
            });
            shutdownExecutorService(executor);
        }
    }

    protected void postEngine() {
        log.info("* TransferBaseEngine postEngine");
    }

    protected void shutdownExecutorService(ExecutorService executor) {
        try {
            executor.shutdown();
            while (!executor.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                Thread.sleep(1000);
            }
        } catch (Exception exception) {
        }
    }

}
