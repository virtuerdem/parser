package com.ttgint.transfer.operation.engine;

import com.ttgint.library.model.Connection;
import com.ttgint.transfer.base.TransferBaseEngine;
import com.ttgint.transfer.base.TransferBaseHandler;
import com.ttgint.transfer.operation.handler.UlEnbCmTransferHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.ArrayList;

@Slf4j
@Component("UL_ENB_CM_TRANSFER")
public class UlEnbCmTransferEngine extends TransferBaseEngine {

    public UlEnbCmTransferEngine(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected TransferBaseHandler getTransferHandler(Connection connection) {
        log.info("* UlEnbCmTransferEngine getTransferHandler");
        return new UlEnbCmTransferHandler(applicationContext, getTransferHandlerRecord(connection));
    }

    @Override
    protected ArrayList<File> getDecompressFiles() {
        return new ArrayList<>(fileLib.readFilesInCurrentPathByContains(engineRecord.getRawPath(), ".xml"));
    }


    @Override
    protected OffsetDateTime getDecompressRecordTime(String fileName) {
        return getDecompressRecordTime(
                fileName
                        .split("_")[1]
                        .substring(0, 10) + " 00:00+03:00",
                "yyyy-MM-dd HH:mmXXX");
    }
}
