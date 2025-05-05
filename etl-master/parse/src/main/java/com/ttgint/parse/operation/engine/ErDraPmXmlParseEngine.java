package com.ttgint.parse.operation.engine;

import com.ttgint.library.enums.ProgressType;
import com.ttgint.library.record.ParseHandlerRecord;
import com.ttgint.library.repository.NetworkNodeRepository;
import com.ttgint.parse.base.ParseBaseEngine;
import com.ttgint.parse.base.ParseBaseHandler;
import com.ttgint.parse.operation.handler.ErDraPmXmlParseHandler;
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
@Component("ER_DRA_PM_XML_PARSE")
public class ErDraPmXmlParseEngine extends ParseBaseEngine {

    private final NetworkNodeRepository networkNodeRepository;

    public ErDraPmXmlParseEngine(ApplicationContext applicationContext,
                                 NetworkNodeRepository networkNodeRepository) {
        super(applicationContext);
        this.networkNodeRepository = networkNodeRepository;
    }

    @Override
    protected void onEngine() {
        Map<String, Long> nodeIds = new HashMap<>();
        networkNodeRepository
                .findByFlowIdAndIsActive(engineRecord.getFlowId(), true)
                .forEach(e -> nodeIds.put(e.getNodeName(), e.getNodeId()));

        ArrayList<File> files
                = new ArrayList<>(fileLib.readFilesInWalkingPathByPostfix(engineRecord.getRawPath(), ".xml"));
        log.info("* ErDraPmXmlParseEngine onEngine fileSize: {}", files.size());

        ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getOnParseThreadCount());
        files.forEach(file -> {
            try {
                ParseBaseHandler handler = new ErDraPmXmlParseHandler(applicationContext,
                        ParseHandlerRecord.getRecord(engineRecord, file, ProgressType.TEST),
                        nodeIds);
                executor.execute(handler);
            } catch (Exception exception) {
            }
        });
        shutdownExecutorService(executor);
    }

}
