package com.ttgint.parse.base;

import com.ttgint.library.loader.LoaderFactory;
import com.ttgint.library.metadata.GenerateMetadata;
import com.ttgint.library.record.ContentDateReaderRecord;
import com.ttgint.library.record.GenerateMetadataRecord;
import com.ttgint.library.record.LoaderFileRecord;
import com.ttgint.library.record.ParseEngineRecord;
import com.ttgint.library.util.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ParseBaseEngine {

    protected final ApplicationContext applicationContext;
    protected final FileLib fileLib;
    protected final ParseMapper parseMapper;
    protected final AutoCounterDefine autoCounterDefine;
    protected final Writer writer;
    protected final ContentDate contentDate;

    protected ParseEngineRecord engineRecord;

    public ParseBaseEngine(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.fileLib = applicationContext.getBean(FileLib.class);
        this.parseMapper = applicationContext.getBean(ParseMapper.class);
        this.autoCounterDefine = applicationContext.getBean(AutoCounterDefine.class);
        this.writer = applicationContext.getBean(Writer.class);
        this.contentDate = applicationContext.getBean(ContentDate.class);
    }

    public void startEngine(ParseEngineRecord record) {
        log.info("* ParseBaseEngine startEngine");
        this.engineRecord = record;
        preparePaths();
        if (record.getIsActiveFetchTables()) {
            fetchTables();
        }

        getTables();
        if (record.getIsActivePreParse()) {
            preEngine();
        }

        if (record.getIsActiveOnParse()) {
            onEngine();
        }
        writer.closeAllStreams();

        if (record.getIsActivePostParse()) {
            postEngine();
        }

        if (record.getIsActiveAutoCounter()) {
            autoCounterDefine.save(engineRecord);
        }
        autoCounterDefine.clear();

        if (record.getIsActiveDiscoverContentDate()) {
            discoverContentDate();
            contentDate.printDates();
        }

        cleanDuplicateBeforeLoader();

        loader();

        cleanDuplicateAfterLoader();
        callProcedure();
        callAggregate();
        callExport();

    }

    private void preparePaths() {
        log.info("* ParseBaseEngine preparePaths");
        fileLib.createFlowPaths(engineRecord.getFlowCode());
        engineRecord.setRawPath(fileLib.createRawPath(engineRecord.getFlowCode()));
    }

    protected void fetchTables() {
        log.info("* ParseBaseEngine fetchTables");
        new GenerateMetadata(applicationContext, GenerateMetadataRecord.getRecord(engineRecord))
                .generate();
    }

    protected void getTables() {
        parseMapper.getTables(engineRecord.getFlowId());
    }

    protected void preEngine() {
        log.info("* ParseBaseEngine preEngine");
    }

    protected void onEngine() {
        log.info("* ParseBaseEngine onEngine");
    }

    protected void postEngine() {
        log.info("* ParseBaseEngine postEngine");
    }

    protected void discoverContentDate() {
        ArrayList<File> files = new ArrayList<>(fileLib.readFilesInCurrentPathByPostfix(engineRecord.getRawPath(),
                engineRecord.getResultFileExtension()));
        log.info("* ParseBaseEngine discoverContentDate fileSize: {}", files.size());

        ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getDiscoverContentDateThreadCount());
        files.forEach(file -> {
            try {
                executor.execute(
                        new ContentDateReader(applicationContext,
                                new ContentDateReaderRecord().getRecord(
                                        file,
                                        engineRecord,
                                        parseMapper.getMapByTableName(file.getName().split("-")[0]))
                        )
                );
            } catch (Exception exception) {
            }
        });
        shutdownExecutorService(executor);
    }

    protected void cleanDuplicateBeforeLoader() {
        log.info("* ParseBaseEngine cleanDuplicateBeforeLoader");
    }

    protected void loader() {
        ArrayList<File> files = new ArrayList<>(fileLib.readFilesInCurrentPathByPostfix(engineRecord.getRawPath(),
                engineRecord.getResultFileExtension()));
        log.info("* ParseBaseEngine loader fileSize: {}", files.size());

        ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getLoaderThreadCount());
        files.forEach(file -> {
            try {
                executor.execute(
                        new LoaderFactory(
                                applicationContext,
                                new LoaderFileRecord().getRecord(
                                        file,
                                        engineRecord,
                                        parseMapper.getMapByTableName(file.getName().split("-")[0]),
                                        contentDate.getContentDateByFileName(file.getName()))
                        ).getLoader()
                );
            } catch (Exception exception) {
            }
        });
        shutdownExecutorService(executor);
    }


    protected void cleanDuplicateAfterLoader() {
        log.info("* ParseBaseEngine cleanDuplicateAfterLoader");
    }

    protected void callProcedure() {
        log.info("* ParseBaseEngine callProcedure");
    }

    protected void callAggregate() {
        log.info("* ParseBaseEngine callAggregate");
    }

    protected void callExport() {
        log.info("* ParseBaseEngine callExport");
    }

    protected void shutdownExecutorService(ExecutorService executor) {
        try {
            executor.shutdown();
            while (!executor.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                Thread.sleep(1000);
            }
        } catch (Exception exception) {
        }
    }

}