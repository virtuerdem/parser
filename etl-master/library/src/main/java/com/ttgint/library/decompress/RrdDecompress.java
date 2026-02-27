package com.ttgint.library.decompress;

import com.ttgint.library.record.DecompressRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.List;

@Slf4j
public class RrdDecompress extends Decompress {

    public RrdDecompress(ApplicationContext applicationContext, DecompressRecord decompressRecord) {
        super(applicationContext, decompressRecord);
    }

    @Override
    protected List<File> decompress() {
        File targetFile = new File(
                (decompressRecord.getTargetPath() + "/" +
                        (decompressRecord.getFileId() != null
                                && !decompressRecord.getSourceFile().getName().contains("^^") ?
                                decompressRecord.getFileId() + "^^" : "") +
                        (decompressRecord.getFileNamePrefix() != null ?
                                decompressRecord.getFileNamePrefix() + ";;" : "") +
                        decompressRecord.getTargetFileName())
                        .replace("//", "/"));
        try {
            Process proc = Runtime.getRuntime()
                    .exec("./rrdtool dump " +
                            decompressRecord.getSourceFile().getAbsolutePath() + " " +
                            targetFile.getAbsolutePath());
            proc.waitFor();
            proc.destroy();

            fileList.add(targetFile);
            insertResult(targetFile);
        } catch (Exception exception) {
            insertError("RRD001", targetFile.getName(), exception.getMessage());
            deleteFile(targetFile);
        }
        deleteFile(decompressRecord.getSourceFile());
        return fileList;
    }

}
