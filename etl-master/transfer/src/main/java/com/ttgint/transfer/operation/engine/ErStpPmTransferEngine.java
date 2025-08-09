package com.ttgint.transfer.operation.engine;

import com.ttgint.library.model.Connection;
import com.ttgint.transfer.base.TransferBaseEngine;
import com.ttgint.transfer.base.TransferBaseHandler;
import com.ttgint.transfer.operation.handler.ErStpPmTransferHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.OffsetDateTime;
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
    protected OffsetDateTime getDecompressRecordTime(String fileName) {
        return getDecompressRecordTime(
                fileName
                        .split("A")[1]
                        .substring(0, 18),
                "yyyyMMdd.HHmmZ");
    }
}
