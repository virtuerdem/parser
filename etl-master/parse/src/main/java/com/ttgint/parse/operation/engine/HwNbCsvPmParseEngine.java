package com.ttgint.parse.operation.engine;

import com.ttgint.library.enums.ProgressType;
import com.ttgint.library.record.ParseHandlerRecord;
import com.ttgint.parse.base.ParseBaseEngine;
import com.ttgint.parse.base.ParseBaseHandler;
import com.ttgint.parse.operation.handler.HwNbCsvPmParseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component("HW_NB_CSV_PM_PARSE")
public class HwNbCsvPmParseEngine extends ParseBaseEngine {

    public HwNbCsvPmParseEngine(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected void onEngine() {
        log.info("* HwNbCsvPmParseEngine onEngine");

        ArrayList<File> files
                = new ArrayList<>(fileLib.readFilesInWalkingPathByPostfix(engineRecord.getRawPath(), ".csv"));
        log.info("* HwNbCsvPmParseEngine onEngine fileSize: {}", files.size());

        ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getOnParseThreadCount());
        files.forEach(file -> {
            try {
                ParseBaseHandler handler = new HwNbCsvPmParseHandler(
                        applicationContext,
                        ParseHandlerRecord.getRecord(engineRecord, file, ProgressType.TEST));
                executor.execute(handler);
            } catch (Exception exception) {
                log.error("! HwNbCsvPmParseEngine handler creation/execution failed for file: {}",
                        file.getName(), exception);
            }
        });
        shutdownExecutorService(executor);
    }
}
