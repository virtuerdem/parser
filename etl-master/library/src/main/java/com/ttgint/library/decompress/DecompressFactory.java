package com.ttgint.library.decompress;

import com.ttgint.library.record.DecompressRecord;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@Getter
@Slf4j
public class DecompressFactory {

    private final Decompress decompress;

    public DecompressFactory(ApplicationContext applicationContext, DecompressRecord decompressRecord) {
        if (decompressRecord.getSourceFile().getName().toLowerCase().endsWith(".asn1")) {
            targetFileNameCheck(decompressRecord, 4);
            decompress = new Asn1Decompress(applicationContext, decompressRecord);
        } else if (decompressRecord.getSourceFile().getName().toLowerCase().endsWith(".gz")) {
            targetFileNameCheck(decompressRecord, 3);
            decompress = new GzDecompress(applicationContext, decompressRecord);
        } else if (decompressRecord.getSourceFile().getName().toLowerCase().endsWith(".rrd")) {
            targetFileNameCheck(decompressRecord, 4);
            decompress = new RrdDecompress(applicationContext, decompressRecord);
        } else if (decompressRecord.getSourceFile().getName().toLowerCase().endsWith(".tar.gz")) {
            targetFileNameCheck(decompressRecord, 7);
            decompress = new TarGzDecompress(applicationContext, decompressRecord);
        } else if (decompressRecord.getSourceFile().getName().toLowerCase().endsWith(".tgz")) {
            targetFileNameCheck(decompressRecord, 4);
            decompress = new TgzDecompress(applicationContext, decompressRecord);
        } else if (decompressRecord.getSourceFile().getName().toLowerCase().endsWith(".z")) {
            targetFileNameCheck(decompressRecord, 2);
            decompress = new ZDecompress(applicationContext, decompressRecord);
        } else if (decompressRecord.getSourceFile().getName().toLowerCase().endsWith(".zip")) {
            targetFileNameCheck(decompressRecord, 4);
            decompress = new ZipDecompress(applicationContext, decompressRecord);
        } else {
            decompress = new DefaultDecompress(applicationContext, decompressRecord);
        }
    }

    private void targetFileNameCheck(DecompressRecord decompressRecord, Integer extentionLength) {
        if (decompressRecord.getSourceFile().getName().equals(decompressRecord.getTargetFileName())) {
            decompressRecord.setTargetFileName(
                    decompressRecord.getTargetFileName()
                            .substring(0, decompressRecord.getTargetFileName().length() - extentionLength));
        }
    }

}
