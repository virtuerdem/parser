package com.ttgint.parse.operation.engine;

import com.ttgint.library.enums.ProgressType;
import com.ttgint.library.record.ParseHandlerRecord;
import com.ttgint.parse.base.ParseBaseEngine;
import com.ttgint.parse.base.ParseBaseHandler;
import com.ttgint.parse.operation.handler.HwMwCsvPmParseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component("HW_MW_PM_PARSE")
public class HwMwCsvPmParseEngine extends ParseBaseEngine {

    public HwMwCsvPmParseEngine(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected void onEngine() {
        log.info("* HwMwCsvPmParseEngine onEngine");

        ArrayList<File> files
                = new ArrayList<>(fileLib.readFilesInWalkingPathByPostfix(engineRecord.getRawPath(), ".csv"));
        log.info("* HwMwCsvPmParseEngine onEngine fileSize: {}", files.size());

        ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getOnParseThreadCount());
        files.forEach(file -> {
            try {
                ParseBaseHandler handler = new HwMwCsvPmParseHandler(
                        applicationContext,
                        ParseHandlerRecord.getRecord(engineRecord, file, ProgressType.TEST));
                executor.execute(handler);
            } catch (Exception exception) {
                log.error("! HwMwCsvPmParseEngine handler creation/execution failed for file: {}",
                        file.getName(), exception);
            }
        });
        shutdownExecutorService(executor);
    }
}
