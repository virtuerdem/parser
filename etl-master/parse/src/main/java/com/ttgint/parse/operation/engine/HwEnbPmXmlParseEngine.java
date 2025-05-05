package com.ttgint.parse.operation.engine;

import com.ttgint.library.enums.ProgressType;
import com.ttgint.library.record.ParseHandlerRecord;
import com.ttgint.library.repository.NetworkNodeRepository;
import com.ttgint.parse.base.ParseBaseEngine;
import com.ttgint.parse.base.ParseBaseHandler;
import com.ttgint.parse.operation.handler.HwEnbPmXmlParseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component("HW_ENB_PM_XML_PARSE")
public class HwEnbPmXmlParseEngine extends ParseBaseEngine {

    private final NetworkNodeRepository networkNodeRepository;

    public HwEnbPmXmlParseEngine(ApplicationContext applicationContext,
                                 NetworkNodeRepository networkNodeRepository) {
        super(applicationContext);
        this.networkNodeRepository = networkNodeRepository;
    }

    @Override
    protected void onEngine() {
        Map<String, Long> nodeIds = new HashMap<>();
        networkNodeRepository
                .findByBranchIdAndIsActive(engineRecord.getBranchId(), true)
                .forEach(e -> nodeIds.put(e.getNodeName(), e.getNodeId()));

        ArrayList<File> files
                = new ArrayList<>(fileLib.readFilesInWalkingPathByPostfix(engineRecord.getRawPath(), ".xml"));
        log.info("* HwEnbPmXmlParseEngine onEngine fileSize: {}", files.size());

        ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getOnParseThreadCount());
        files.forEach(file -> {
            try {
                ParseBaseHandler handler = new HwEnbPmXmlParseHandler(applicationContext,
                        ParseHandlerRecord.getRecord(engineRecord, file, ProgressType.TEST),
                        nodeIds);
                executor.execute(handler);
            } catch (Exception exception) {
            }
        });
        shutdownExecutorService(executor);
    }

}
