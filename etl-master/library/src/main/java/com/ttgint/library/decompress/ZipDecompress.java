package com.ttgint.library.decompress;

import com.ttgint.library.record.DecompressRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.springframework.context.ApplicationContext;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

@Slf4j
public class ZipDecompress extends Decompress {

    public ZipDecompress(ApplicationContext applicationContext, DecompressRecord decompressRecord) {
        super(applicationContext, decompressRecord);
    }

    @Override
    protected void decompress() {
        try (ZipArchiveInputStream in =
                     new ZipArchiveInputStream(
                             new FileInputStream(decompressRecord.getSourceFile()))) {
            ZipArchiveEntry entry;
            while ((entry = in.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    File targetFile = new File(
                            (decompressRecord.getTargetPath() + "/" +
                                    (decompressRecord.getFileId() != null
                                            && !decompressRecord.getSourceFile().getName().contains("^^") ?
                                            decompressRecord.getFileId() + "^^" : "") +
                                    (decompressRecord.getFileNamePrefix() != null ?
                                            decompressRecord.getFileNamePrefix() + ";;" : "") +
                                    entry.getName())
                                    .replace("//", "/"));
                    try (BufferedOutputStream out =
                                 new BufferedOutputStream(
                                         new FileOutputStream(targetFile))) {
                        int len;
                        byte[] buffer = new byte[1024];
                        while ((len = in.read(buffer)) > 0) {
                            out.write(buffer, 0, len);
                        }

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
    }

}
