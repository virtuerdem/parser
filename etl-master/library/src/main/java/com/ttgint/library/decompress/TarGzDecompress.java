package com.ttgint.library.decompress;

import com.ttgint.library.record.DecompressRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.springframework.context.ApplicationContext;

import java.io.*;

@Slf4j
public class TarGzDecompress extends Decompress {

    public TarGzDecompress(ApplicationContext applicationContext, DecompressRecord decompressRecord) {
        super(applicationContext, decompressRecord);
    }

    @Override
    protected void decompress() {
        try (TarArchiveInputStream in =
                     new TarArchiveInputStream(
                             new GzipCompressorInputStream(
                                     new BufferedInputStream(
                                             new FileInputStream(decompressRecord.getSourceFile()))))) {
            TarArchiveEntry entry;
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
                        insertError("TARGZ001", targetFile.getName(), exception.getMessage());
                        deleteFile(targetFile);
                    }
                }
            }
        } catch (Exception exception) {
            insertError("TARGZ002", null, exception.getMessage());
        }
        deleteFile(decompressRecord.getSourceFile());
    }

}
