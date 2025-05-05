package com.ttgint.library.decompress;

import com.ttgint.library.record.DecompressRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.springframework.context.ApplicationContext;

import java.io.*;

@Slf4j
public class GzDecompress extends Decompress {

    public GzDecompress(ApplicationContext applicationContext, DecompressRecord decompressRecord) {
        super(applicationContext, decompressRecord);
    }

    @Override
    protected void decompress() {
        File targetFile = new File(
                (decompressRecord.getTargetPath() + "/" +
                        (decompressRecord.getFileId() != null
                                && !decompressRecord.getSourceFile().getName().contains("^^") ?
                                decompressRecord.getFileId() + "^^" : "") +
                        (decompressRecord.getFileNamePrefix() != null ?
                                decompressRecord.getFileNamePrefix() + ";;" : "") +
                        decompressRecord.getTargetFileName())
                        .replace("//", "/"));
        try (GzipCompressorInputStream in =
                     new GzipCompressorInputStream(
                             new BufferedInputStream(
                                     new FileInputStream(decompressRecord.getSourceFile())));
             BufferedOutputStream out =
                     new BufferedOutputStream(
                             new FileOutputStream(targetFile))) {
            int len;
            byte[] buffer = new byte[1024];
            while ((len = in.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            insertResult(targetFile);
        } catch (Exception exception) {
            insertError("GZ001", targetFile.getName(), exception.getMessage());
            deleteFile(targetFile);
        }
        deleteFile(decompressRecord.getSourceFile());
    }

}
