package com.ttgint.library.decompress;

import com.ttgint.library.record.DecompressRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.springframework.context.ApplicationContext;

import java.io.*;
import java.util.List;

@Slf4j
public class ZipDecompress extends Decompress {

    public ZipDecompress(ApplicationContext applicationContext, DecompressRecord decompressRecord) {
        super(applicationContext, decompressRecord);
    }

    @Override
    protected List<File> decompress() {
        try (FileInputStream fileInputStream = new FileInputStream(decompressRecord.getSourceFile());
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             ZipArchiveInputStream zipArchiveInputStream = new ZipArchiveInputStream(bufferedInputStream)) {
            ZipArchiveEntry entry;
            while ((entry = zipArchiveInputStream.getNextEntry()) != null) {
                if (!entry.isDirectory() && checkName(entry.getName())) {
                    File targetFile = getTargetFile(entry.getName());
                    try (FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
                         BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {
                        int len;
                        byte[] buffer = new byte[1024];
                        while ((len = zipArchiveInputStream.read(buffer)) > 0) {
                            bufferedOutputStream.write(buffer, 0, len);
                        }

                        fileList.add(targetFile);
                        insertResult(targetFile);
                    } catch (Exception exception) {
                        insertError("ZIP001", targetFile.getName(), exception.getMessage());
                        deleteFile(targetFile);
                    }
                }
            }
        } catch (Exception exception) {
            insertError("ZIP002", null, exception.getMessage());
        }
        deleteFile(decompressRecord.getSourceFile());
        return fileList;
    }

    private Boolean checkName(String fileName) {
        boolean checkStartWith = true;
        if (decompressRecord.getSourceStartWith() != null && !decompressRecord.getSourceStartWith().trim().isEmpty()) {
            checkStartWith = fileName.startsWith(decompressRecord.getSourceStartWith());
        }

        boolean checkContains = true;
        if (decompressRecord.getSourceContains() != null && !decompressRecord.getSourceContains().trim().isEmpty()) {
            checkContains = fileName.contains(decompressRecord.getSourceContains());
        }

        boolean checkEndWith = true;
        if (decompressRecord.getSourceEndWith() != null && !decompressRecord.getSourceEndWith().trim().isEmpty()) {
            checkEndWith = fileName.endsWith(decompressRecord.getSourceEndWith());
        }

        return checkStartWith && checkContains && checkEndWith;
    }

    @Override
    protected File getTargetFile(String fileName) {
        return new File(
                (decompressRecord.getTargetPath() + "/" +
                        (decompressRecord.getFileId() != null
                                && !decompressRecord.getSourceFile().getName().contains("^^") ?
                                decompressRecord.getFileId() + "^^" : "") +
                        getFileNamePrefix(fileName) +
                        fileName)
                        .replace("//", "/"));
    }

    private String getFileNamePrefix(String fileName) {
        String zip = (decompressRecord.getSourceFile().getName().contains("^^") ?
                decompressRecord.getSourceFile().getName().split("\\^")[2] :
                decompressRecord.getSourceFile().getName())
                .replace(".zip", "");
        String file = fileName
                .substring(0,
                        (fileName.lastIndexOf(".") < 0 ?
                                fileName.length() :
                                fileName.lastIndexOf(".")));
        return (decompressRecord.getFileNamePrefix() != null ?
                decompressRecord.getFileNamePrefix() + ";;" :
                (zip.equals(file) ? "" :
                        decompressRecord.getSourceFile().getName()
                                .replace(".zip", ";;"))
        );
    }
}
