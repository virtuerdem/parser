package com.ttgint.parse.operation.engine;

import com.ttgint.library.enums.ProgressType;
import com.ttgint.library.record.ParseHandlerRecord;
import com.ttgint.parse.base.ParseBaseEngine;
import com.ttgint.parse.base.ParseBaseHandler;
import com.ttgint.parse.operation.handler.DefaultTxtHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component("DEFAULT_TXT_PARSE")
public class DefaultTxtEngine extends ParseBaseEngine {

    public DefaultTxtEngine(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected void preEngine() {
        log.info("* DefaultTxtEngine preEngine");
    }

    @Override
    protected void onEngine() {
        log.info("* DefaultTxtEngine onEngine");

        ArrayList<File> files
                = new ArrayList<>(fileLib.readFilesInWalkingPathByPostfix(engineRecord.getRawPath(), ".txt"));
        log.info("* DefaultTxtEngine onEngine fileSize: {}", files.size());

        ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getOnParseThreadCount());
        files.forEach(file -> {
            try {
                ParseBaseHandler handler = new DefaultTxtHandler(applicationContext,
                        ParseHandlerRecord.getRecord(engineRecord, file, ProgressType.PRODUCT));
                executor.execute(handler);
            } catch (Exception exception) {
            }
        });
        shutdownExecutorService(executor);
    }

    @Override
    protected void postEngine() {
        log.info("* DefaultTxtEngine postEngine");
    }

}
