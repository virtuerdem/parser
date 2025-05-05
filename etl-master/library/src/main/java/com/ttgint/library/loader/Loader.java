package com.ttgint.library.loader;

import com.ttgint.library.model.LoaderResult;
import com.ttgint.library.record.LoaderFileRecord;
import com.ttgint.library.repository.LoaderResultRepository;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.File;
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

    private Long loadedCount = 0L;
    private Long errorCount = 0L;

    public Loader(ApplicationContext applicationContext, LoaderFileRecord loaderFileRecord) {
        this.applicationContext = applicationContext;
        this.loaderFileRecord = loaderFileRecord;
        this.loaderResultRepository = applicationContext.getBean(LoaderResultRepository.class);
    }

    @Override
    public void run() {
        beforeLoader();
        loader();
        insertResults();
        afterLoader();
        printResult();
    }

    public void beforeLoader() {
    }

    public abstract void loader();

    public void afterLoader() {
        try {
            if (loadedCount > 0) {
                afterLoaderSuccess();
            } else {
                afterLoaderFailure();
            }
        } catch (Exception exception) {
            log.error("*Loader afterLoader exception {}", exception.getMessage().replace("\n", ""));
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
        if (loadedCount > 0 && loaderFileRecord.getNeedLoaderResult()) {
            loaderFileRecord
                    .getContentDates()
                    .forEach(e -> insertResult(e.getFragmentDate(), e.getRowCount()));
        }
    }

    public void insertResult(OffsetDateTime fragmentDate, Long rowCount) {
        loaderResultRepository
                .findByFlowIdAndSchemaNameAndTableNameAndFragmentDate(
                        getLoaderFileRecord().getFlowId(),
                        getLoaderFileRecord().getSchemaName(),
                        getLoaderFileRecord().getTableName(),
                        fragmentDate)
                .ifPresentOrElse(result -> {
                            result.setRowCount(result.getRowCount() + rowCount);
                            result.setLoadTryCount(result.getLoadTryCount() + 1);
                            result.setLoadedTime(OffsetDateTime.now());
                            loaderResultRepository.save(result);
                        },
                        () -> loaderResultRepository.save(LoaderResult
                                .getEntity(loaderFileRecord, fragmentDate, rowCount)));
    }

    private void printResult() {
        if (loadedCount > 0) {
            log.info("> Loader [true] : {}", loaderFileRecord.getFile().getName());
        } else {
            log.error("! Loader [false] : {}", loaderFileRecord.getFile().getName());
        }
    }

}
