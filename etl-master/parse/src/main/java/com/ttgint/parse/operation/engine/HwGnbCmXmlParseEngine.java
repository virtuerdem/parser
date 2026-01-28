package com.ttgint.parse.operation.engine;

import com.ttgint.library.enums.ProgressType;
import com.ttgint.library.record.ParseHandlerRecord;
import com.ttgint.parse.base.ParseBaseEngine;
import com.ttgint.parse.base.ParseBaseHandler;
import com.ttgint.parse.operation.handler.HwGnbCmXmlParseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component("HW_GNB_CM_XML_PARSE")
public class HwGnbCmXmlParseEngine extends ParseBaseEngine {

    public HwGnbCmXmlParseEngine(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected void onEngine() {
        Map<String, Long> nodeIds = getNetworkNodesByBranchId();
        log.info("* HwGnbCmXmlParseEngine onEngine activeNodeSize: {}", nodeIds.size());

        ArrayList<File> files
                = new ArrayList<>(fileLib.readFilesInWalkingPathByPostfix(engineRecord.getRawPath(), ".xml"));
        log.info("* HwGnbCmXmlParseEngine onEngine fileSize: {}", files.size());

        ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getOnParseThreadCount());
        for (File file : files) {
            try {
                ParseBaseHandler handler = new HwGnbCmXmlParseHandler(applicationContext,
                        ParseHandlerRecord.getRecord(engineRecord, file, ProgressType.TEST),
                        nodeIds);
                executor.execute(handler);
            } catch (Exception exception) {
            }
        }
        shutdownExecutorService(executor);
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
