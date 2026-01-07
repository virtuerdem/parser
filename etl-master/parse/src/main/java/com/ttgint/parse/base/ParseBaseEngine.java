package com.ttgint.parse.base;

import com.ttgint.library.decompress.DecompressFactory;
import com.ttgint.library.loader.LoaderFactory;
import com.ttgint.library.metadata.GenerateMetadata;
import com.ttgint.library.model.NetworkItem;
import com.ttgint.library.model.NetworkNode;
import com.ttgint.library.record.*;
import com.ttgint.library.repository.NetworkItemRepository;
import com.ttgint.library.repository.NetworkNodeRepository;
import com.ttgint.library.util.*;
import com.ttgint.library.validation.XmlValidation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
public class ParseBaseEngine {

    protected final ApplicationContext applicationContext;
    protected final FileLib fileLib;
    protected final ParseMapper parseMapper;
    protected final AutoCounterDefine autoCounterDefine;
    protected final Writer writer;
    protected final ContentDate contentDate;

    protected final NetworkNodeRepository networkNodeRepository;
    protected final NetworkItemRepository networkItemRepository;

    protected ParseEngineRecord engineRecord;

    public ParseBaseEngine(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.fileLib = applicationContext.getBean(FileLib.class);
        this.parseMapper = applicationContext.getBean(ParseMapper.class);
        this.autoCounterDefine = applicationContext.getBean(AutoCounterDefine.class);
        this.writer = applicationContext.getBean(Writer.class);
        this.contentDate = applicationContext.getBean(ContentDate.class);

        this.networkNodeRepository = applicationContext.getBean(NetworkNodeRepository.class);
        this.networkItemRepository = applicationContext.getBean(NetworkItemRepository.class);
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

        if (record.getIsActiveCleanDuplicateApp()) {
            cleanDuplicateApp();
        }

        if (record.getIsActiveLoader()) {
            loader();
        }

        if (record.getIsActiveCleanDuplicateProc()) {
            cleanDuplicateProc();
        }

        if (record.getIsActiveCallProcedure()) {
            callProcedure();
        }

        if (record.getIsActiveCallAggregate()) {
            callAggregate();
        }

        if (record.getIsActiveCallExport()) {
            callExport();
        }
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

    protected OffsetDateTime getDecompressRecordTime(String offsetDateTime, String dateTimeFormatter) {
        return OffsetDateTime.parse(offsetDateTime, DateTimeFormatter.ofPattern(dateTimeFormatter));
    }

    protected OffsetDateTime getDecompressRecordTime(String fileName) {
        return null;
    }

    protected DecompressRecord getDecompressRecord(File file) {
        return DecompressRecord.getRecord(engineRecord,
                file,
                getDecompressRecordTime(file.getName()),
                (file.getName().contains("^^") ? file.getName().split("\\^")[0] : null),
                null,
                file.getName());
    }

    protected List<File> getDecompressedFiles(File file) {
        return new DecompressFactory(applicationContext, getDecompressRecord(file)).getDecompress().getDecompressedFiles();
    }

    protected File getValidatedFile(File file) {
        if (engineRecord.getValidation() != null && engineRecord.getValidation() && file.getName().endsWith(".xml")) {
            return new XmlValidation(file.getAbsolutePath()).getValidatedFile();
        } else {
            return file;
        }
    }

    protected List<File> prepareFile(File file) {
        if (engineRecord.getHavePrepareFileFeature() != null && engineRecord.getHavePrepareFileFeature()) {
            List<File> preparedFiles = new ArrayList<>();
            if (engineRecord.getDecompress() != null && engineRecord.getDecompress()) {
                for (File f : getDecompressedFiles(file)) {
                    preparedFiles.add(getValidatedFile(f));
                }
            } else {
                preparedFiles.add(getValidatedFile(file));
            }
            return preparedFiles;
        } else {
            return Collections.singletonList(file);
        }
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

    protected void cleanDuplicateApp() {
        log.info("* ParseBaseEngine cleanDuplicateApp");
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

    protected void cleanDuplicateProc() {
        log.info("* ParseBaseEngine cleanDuplicateProc");
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

    protected Map<String, Long> getNetworkNodesByFlowId() {
        return networkNodeRepository
                .findByFlowIdAndIsActive(engineRecord.getFlowId(), true)
                .stream()
                .collect(Collectors.toMap(NetworkNode::getNodeName, NetworkNode::getNodeId));
    }

    protected Map<String, Long> getNetworkNodesByBranchId() {
        return networkNodeRepository
                .findByBranchIdAndIsActive(engineRecord.getBranchId(), true)
                .stream()
                .collect(Collectors.toMap(NetworkNode::getNodeName, NetworkNode::getNodeId));
    }

    protected Map<String, Long> getNetworkItemsByFlowId() {
        return networkItemRepository
                .findByFlowIdAndIsActive(engineRecord.getFlowId(), true)
                .stream()
                .collect(Collectors.toMap(NetworkItem::getItemName, NetworkItem::getItemId));
    }

    protected Map<String, Long> getNetworkItemsByBranchId() {
        return networkItemRepository
                .findByBranchIdAndIsActive(engineRecord.getBranchId(), true)
                .stream()
                .collect(Collectors.toMap(NetworkItem::getItemName, NetworkItem::getItemId));
    }

}
