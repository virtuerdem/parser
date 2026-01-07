package com.ttgint.parse.operation.engine;

import com.ttgint.library.enums.ProgressType;
import com.ttgint.library.record.ParseHandlerRecord;
import com.ttgint.parse.base.ParseBaseEngine;
import com.ttgint.parse.base.ParseBaseHandler;
import com.ttgint.parse.operation.handler.HwRncConfXmlParseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component("HW_RNC_CONF_XML_PARSE")
public class HwRncConfXmlParseEngine extends ParseBaseEngine {

    public HwRncConfXmlParseEngine(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected void onEngine() {
        Map<String, Long> nodeIds = getNetworkNodesByBranchId();
        log.info("* HwRncConfXmlParseEngine onEngine activeNodeSize: {}", nodeIds.size());

        ArrayList<File> files
                = new ArrayList<>(fileLib.readFilesInWalkingPathByPostfix(engineRecord.getRawPath(), ".xml"));
        log.info("* HwRncConfXmlParseEngine onEngine fileSize: {}", files.size());

        ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getOnParseThreadCount());
        files.forEach(file -> {
            try {
                ParseBaseHandler handler = new HwRncConfXmlParseHandler(applicationContext,
                        ParseHandlerRecord.getRecord(engineRecord, file, ProgressType.TEST),
                        nodeIds);
                executor.execute(handler);
            } catch (Exception exception) {
            }
        });
        shutdownExecutorService(executor);
    }

}
