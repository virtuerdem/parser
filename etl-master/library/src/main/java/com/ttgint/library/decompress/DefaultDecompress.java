package com.ttgint.library.decompress;

import com.ttgint.library.record.DecompressRecord;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.List;

public class DefaultDecompress extends Decompress {

    public DefaultDecompress(ApplicationContext applicationContext, DecompressRecord decompressRecord) {
        super(applicationContext, decompressRecord);
    }

    @Override
    protected List<File> decompress() {
        //no need to decompress the file, just check for DecompressResult
        fileList.add(decompressRecord.getSourceFile());
        insertResult(decompressRecord.getSourceFile());
        return fileList;
    }

}
