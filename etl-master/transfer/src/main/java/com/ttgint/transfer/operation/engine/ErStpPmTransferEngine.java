package com.ttgint.transfer.operation.engine;

import com.ttgint.library.model.Connection;
import com.ttgint.library.record.DecompressRecord;
import com.ttgint.transfer.base.TransferBaseEngine;
import com.ttgint.transfer.base.TransferBaseHandler;
import com.ttgint.transfer.operation.handler.ErStpPmTransferHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Slf4j
@Component("ER_STP_PM_TRANSFER")
public class ErStpPmTransferEngine extends TransferBaseEngine {

    public ErStpPmTransferEngine(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected TransferBaseHandler getTransferHandler(Connection connection) {
        log.info("* ErStpPmTransferEngine getTransferHandler");
        return new ErStpPmTransferHandler(applicationContext, getTransferHandlerRecord(connection));
    }

    @Override
    protected ArrayList<File> getDecompressFiles() {
        return new ArrayList<>(fileLib.readFilesInCurrentPathByPostfix(engineRecord.getRawPath(), ".asn1"));
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
