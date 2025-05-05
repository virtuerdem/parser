package com.ttgint.parse.operation.engine;

import com.ttgint.library.enums.ProgressType;
import com.ttgint.library.record.ParseHandlerRecord;
import com.ttgint.parse.base.ParseBaseEngine;
import com.ttgint.parse.base.ParseBaseHandler;
import com.ttgint.parse.operation.handler.DefaultTextHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component("DEFAULT_TEXT_PARSE")
public class DefaultTextEngine extends ParseBaseEngine {

    public DefaultTextEngine(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected void preEngine() {
        log.info("* DefaultTextEngine preEngine");
    }

    @Override
    protected void onEngine() {
        log.info("* DefaultTextEngine onEngine");

        ArrayList<File> files
                = new ArrayList<>(fileLib.readFilesInWalkingPathByPostfix(engineRecord.getRawPath(), ".txt"));
        log.info("* DefaultTextEngine onEngine fileSize: {}", files.size());

        ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getOnParseThreadCount());
        files.forEach(file -> {
            try {
                ParseBaseHandler handler = new DefaultTextHandler(applicationContext,
                        ParseHandlerRecord.getRecord(engineRecord, file, ProgressType.PRODUCT),
                        ",");
                executor.execute(handler);
            } catch (Exception exception) {
            }
        });
        shutdownExecutorService(executor);
    }

    @Override
    protected void postEngine() {
        log.info("* DefaultTextEngine postEngine");
    }

}
