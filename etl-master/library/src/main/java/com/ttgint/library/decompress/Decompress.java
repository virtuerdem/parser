package com.ttgint.library.decompress;

import com.ttgint.library.model.DecompressError;
import com.ttgint.library.model.DecompressResult;
import com.ttgint.library.record.DecompressRecord;
import com.ttgint.library.repository.DecompressErrorRepository;
import com.ttgint.library.repository.DecompressResultRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public abstract class Decompress implements Runnable {

    private final DecompressResultRepository decompressResultRepository;
    private final DecompressErrorRepository decompressErrorRepository;
    protected final DecompressRecord decompressRecord;

    public Decompress(ApplicationContext applicationContext, DecompressRecord decompressRecord) {
        this.decompressResultRepository = applicationContext.getBean(DecompressResultRepository.class);
        this.decompressErrorRepository = applicationContext.getBean(DecompressErrorRepository.class);
        this.decompressRecord = decompressRecord;
    }

    @Override
    public void run() {
        decompress();
    }

    protected abstract void decompress();

    protected void deleteFile(File file) {
        try {
            if (file.exists() && !file.isDirectory()) {
                Files.delete(Paths.get(file.getAbsolutePath()));
            }
        } catch (Exception exception) {
            insertError("DEL001", null, exception.getMessage());
        }
    }

    protected void insertResult(File targetFile) {
        if (decompressRecord.getNeedResult()) {
            decompressResultRepository.save(DecompressResult.recordToEntity(decompressRecord, targetFile));
        }
    }

    protected void insertError(String errorCode, String targetFile, String errorMessage) {
        log.error("! Decompress errorCode:{} sourceFile:{} targetFile:{} message:{}",
                errorCode, decompressRecord.getSourceFile().getName(), targetFile, errorMessage);
        decompressErrorRepository.save(DecompressError.recordToEntity(decompressRecord, errorCode, targetFile, errorMessage));
    }

}
