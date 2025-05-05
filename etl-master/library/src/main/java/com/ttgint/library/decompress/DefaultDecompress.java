package com.ttgint.library.decompress;

import com.ttgint.library.record.DecompressRecord;
import org.springframework.context.ApplicationContext;

public class DefaultDecompress extends Decompress {

    public DefaultDecompress(ApplicationContext applicationContext, DecompressRecord decompressRecord) {
        super(applicationContext, decompressRecord);
    }

    @Override
    protected void decompress() {
        //no need to decompress the file, just check for DecompressResult
        insertResult(decompressRecord.getSourceFile());
    }

}
