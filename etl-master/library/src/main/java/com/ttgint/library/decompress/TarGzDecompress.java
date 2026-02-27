package com.ttgint.library.decompress;

import com.ttgint.library.record.DecompressRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.springframework.context.ApplicationContext;

import java.io.*;
import java.util.List;

@Slf4j
public class TarGzDecompress extends Decompress {

    public TarGzDecompress(ApplicationContext applicationContext, DecompressRecord decompressRecord) {
        super(applicationContext, decompressRecord);
    }

    @Override
    protected List<File> decompress() {
        try (FileInputStream fileInputStream = new FileInputStream(decompressRecord.getSourceFile());
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             GzipCompressorInputStream gzipCompressorInputStream = new GzipCompressorInputStream(bufferedInputStream);
             TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(gzipCompressorInputStream)) {
            TarArchiveEntry entry;
            while ((entry = tarArchiveInputStream.getNextEntry()) != null) {
                if (!entry.isDirectory()) {
                    File targetFile = getTargetFile(entry.getName());
                    try (FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
                         BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {
                        int len;
                        byte[] buffer = new byte[1024];
                        while ((len = tarArchiveInputStream.read(buffer)) > 0) {
                            bufferedOutputStream.write(buffer, 0, len);
                        }

                        fileList.add(targetFile);
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
        return fileList;
    }

}
