package com.ttgint.library.loader;

import com.ttgint.library.model.LoaderHistory;
import com.ttgint.library.model.LoaderResult;
import com.ttgint.library.model.LoaderResultHistory;
import com.ttgint.library.record.ContentDateResultRecord;
import com.ttgint.library.record.LoaderFileRecord;
import com.ttgint.library.repository.LoaderHistoryRepository;
import com.ttgint.library.repository.LoaderResultHistoryRepository;
import com.ttgint.library.repository.LoaderResultRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.time.OffsetDateTime;

@Getter
@Setter
@Slf4j
public abstract class Loader implements Runnable {

    private final ApplicationContext applicationContext;
    private final LoaderFileRecord loaderFileRecord;

    private final LoaderResultRepository loaderResultRepository;
    private final LoaderHistoryRepository loaderHistoryRepository;
    private final LoaderResultHistoryRepository loaderResultHistoryRepository;

    private Long loadedCount = 0L;
    private Long errorCount = 0L;

    public Loader(ApplicationContext applicationContext, LoaderFileRecord loaderFileRecord) {
        this.applicationContext = applicationContext;
        this.loaderFileRecord = loaderFileRecord;
        this.loaderResultRepository = applicationContext.getBean(LoaderResultRepository.class);
        this.loaderHistoryRepository = applicationContext.getBean(LoaderHistoryRepository.class);
        this.loaderResultHistoryRepository = applicationContext.getBean(LoaderResultHistoryRepository.class);
    }

    @Override
    public void run() {
        LoaderHistory loaderHistory = LoaderHistory.getEntity(loaderFileRecord);
        loaderHistory.setTotalRowCount(rowCount());
        loaderFileRecord.getLoaderEnvironment().setSqlLdrErrorLimit(loaderHistory.getTotalRowCount());
        loaderHistory.setLoadStartTime(OffsetDateTime.now());
        try {
            loader();
            insertResults();
        } catch (Exception exception) {
            log.error("! Loader run fileName: {}", loaderFileRecord.getFile().getName(), exception);
            loaderHistory.setLoadMessage(exception.getMessage());
        }
        afterLoader();
        printResult();

        loaderHistory.setLoadedRowCount(loadedCount);
        loaderHistory.setFailedRowCount(errorCount);
        loaderHistory.setLoadEndTime(OffsetDateTime.now());
        loaderHistory.setIsLoaded(loadedCount > 0);
        loaderHistoryRepository.save(loaderHistory);
    }

    public abstract void loader() throws Exception;

    public void afterLoader() {
        try {
            if (loadedCount > 0) {
                afterLoaderSuccess();
            } else {
                afterLoaderFailure();
            }
        } catch (Exception exception) {
            log.error("! Loader afterLoader fileName: {}", loaderFileRecord.getFile().getName(), exception);
        }
    }

    public void afterLoaderSuccess() throws IOException {
        Files.delete(loaderFileRecord.getFile().toPath());
    }

    public void afterLoaderFailure() throws IOException {
        Files.move(loaderFileRecord.getFile().toPath(),
                prepareFile(getLoaderFileRecord().getLoaderEnvironment().getLocalFailedFolder(), null)
                        .toPath()
        );
    }

    public File prepareFile(String folder, String extension) {
        return new File((loaderFileRecord.getLoaderEnvironment().getLocalRootPath() +
                folder + "/" +
                loaderFileRecord.getFlowCode() + "/" +
                loaderFileRecord.getFlowProcessCode() + "+" +
                (extension == null
                        ? loaderFileRecord.getFile().getName()
                        : loaderFileRecord.getFile().getName()
                        .replace(loaderFileRecord.getFileExtension(), extension)))
                .replace("//", "/"));
    }

    public void insertResults() {
        if (loaderFileRecord.getNeedLoaderResult()) {
            loaderFileRecord
                    .getContentDates()
                    .forEach(e ->
                            insertResult(e.getFragmentDate(),
                                    e.getRowCount(),
                                    (loadedCount > 0 ? e.getRowCount() : 0L),
                                    (loadedCount > 0 ? 0L : e.getRowCount())));
        }
    }

    public void insertResult(OffsetDateTime fragmentDate,
                             Long totalRowCount,
                             Long loadedRowCount,
                             Long failedRowCount) {
        loaderResultRepository
                .findByFlowIdAndSchemaNameAndTableNameAndFragmentDate(
                        loaderFileRecord.getFlowId(),
                        loaderFileRecord.getSchemaName(),
                        loaderFileRecord.getTableName(),
                        fragmentDate)
                .ifPresentOrElse(result -> {
                            result.setTotalRowCount(result.getTotalRowCount() + totalRowCount);
                            result.setLoadedRowCount(result.getLoadedRowCount() + loadedRowCount);
                            result.setFailedRowCount(result.getFailedRowCount() + failedRowCount);
                            result.setLoadTryCount(result.getLoadTryCount() + 1);
                            result.setLoadedTime(OffsetDateTime.now());
                            result.setIsLoaded(result.getTotalRowCount() > 0);
                            loaderResultRepository.save(result);
                        },
                        () -> loaderResultRepository.save(LoaderResult
                                .getEntity(loaderFileRecord, fragmentDate, totalRowCount, loadedRowCount, failedRowCount)));

        loaderResultHistoryRepository.save(
                LoaderResultHistory.getEntity(loaderFileRecord, fragmentDate, totalRowCount, loadedRowCount, failedRowCount));
    }

    private void printResult() {
        if (loadedCount > 0) {
            if (errorCount > 0) {
                log.info("! Loader [part] : {}", loaderFileRecord.getFile().getName());
            } else {
                log.info("> Loader [true] : {}", loaderFileRecord.getFile().getName());
            }
        } else {
            log.error("! Loader [false] : {}", loaderFileRecord.getFile().getName());
        }
    }

    private Long rowCount() {
        long count
                = getLoaderFileRecord().getContentDates().stream().mapToLong(ContentDateResultRecord::getRowCount)
                .sum();
        if (count < 1) {
            try (FileReader fileReader = new FileReader(loaderFileRecord.getFile());
                 BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                while (bufferedReader.readLine() != null) {
                    count++;
                }
            } catch (Exception exception) {
                log.error("! Loader rowCount fileName: {}", loaderFileRecord.getFile().getName(), exception);
            }
        }
        return count;
    }
}
