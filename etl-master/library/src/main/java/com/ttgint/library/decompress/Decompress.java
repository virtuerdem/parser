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
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class Decompress implements Runnable {

    private final DecompressResultRepository decompressResultRepository;
    private final DecompressErrorRepository decompressErrorRepository;
    protected final DecompressRecord decompressRecord;

    protected final List<File> fileList = new ArrayList<>();

    public Decompress(ApplicationContext applicationContext, DecompressRecord decompressRecord) {
        this.decompressResultRepository = applicationContext.getBean(DecompressResultRepository.class);
        this.decompressErrorRepository = applicationContext.getBean(DecompressErrorRepository.class);
        this.decompressRecord = decompressRecord;
    }

    @Override
    public void run() {
        decompress();
    }

    public List<File> getDecompressedFiles() {
        decompress();
        return fileList;
    }

    protected abstract List<File> decompress();

    protected File getTargetFile(String fileName) {
        return new File(
                (decompressRecord.getTargetPath() + "/" +
                        (decompressRecord.getFileId() != null
                                && !decompressRecord.getSourceFile().getName().contains("^^") ?
                                decompressRecord.getFileId() + "^^" : "") +
                        (decompressRecord.getFileNamePrefix() != null ?
                                decompressRecord.getFileNamePrefix() + ";;" : "") +
                        fileName)
                        .replace("//", "/"));
    }

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
