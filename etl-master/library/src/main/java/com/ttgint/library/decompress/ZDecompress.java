package com.ttgint.library.decompress;

import com.ttgint.library.record.DecompressRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.compressors.z.ZCompressorInputStream;
import org.springframework.context.ApplicationContext;

import java.io.*;
import java.util.List;

@Slf4j
public class ZDecompress extends Decompress {

    public ZDecompress(ApplicationContext applicationContext, DecompressRecord decompressRecord) {
        super(applicationContext, decompressRecord);
    }

    @Override
    protected List<File> decompress() {
        File targetFile = getTargetFile(decompressRecord.getTargetFileName());
        try (FileInputStream fileInputStream = new FileInputStream(decompressRecord.getSourceFile());
             BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
             ZCompressorInputStream zCompressorInputStream = new ZCompressorInputStream(bufferedInputStream);
             FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {
            int len;
            byte[] buffer = new byte[1024];
            while ((len = zCompressorInputStream.read(buffer)) > 0) {
                bufferedOutputStream.write(buffer, 0, len);
            }

            fileList.add(targetFile);
            insertResult(targetFile);
        } catch (Exception exception) {
            insertError("Z001", targetFile.getName(), exception.getMessage());
            deleteFile(targetFile);
        }
        deleteFile(decompressRecord.getSourceFile());
        return fileList;
    }

}
