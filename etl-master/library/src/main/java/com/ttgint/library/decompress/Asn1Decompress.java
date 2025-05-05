package com.ttgint.library.decompress;

import com.ttgint.library.record.DecompressRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;

@Slf4j
public class Asn1Decompress extends Decompress {

    public Asn1Decompress(ApplicationContext applicationContext, DecompressRecord decompressRecord) {
        super(applicationContext, decompressRecord);
    }

    @Override
    protected void decompress() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Process proc = Runtime.getRuntime()
                    .exec("perl Asn1Decoder.pl " + decompressRecord.getSourceFile().getAbsolutePath());
            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
                String line = "";
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(" ").append(line);
                }
            }
            proc.waitFor();
            proc.destroy();

            if (!stringBuilder.toString().contains("Completed in ")) {
                throw new Exception();
            }

            Arrays.stream(new File(decompressRecord.getSourceFile().getParent()).listFiles())
                    .filter(e -> e.isFile()
                            && !e.getName().equals(".")
                            && !e.getName().equals("..")
                            && e.getName().endsWith(".csv")
                            && e.getName().startsWith(decompressRecord.getSourceFile().getName().replace(".asn1", "")))
                    .forEach(this::insertResult);
        } catch (Exception exception) {
            insertError("ASN1001", null, stringBuilder.toString());
            Arrays.stream(new File(decompressRecord.getSourceFile().getParent()).listFiles())
                    .filter(e -> e.isFile()
                            && !e.getName().equals(".")
                            && !e.getName().equals("..")
                            && !e.getName().contains(".asn1")
                            && e.getName().startsWith(decompressRecord.getSourceFile().getName().replace(".asn1", "")))
                    .forEach(this::deleteFile);
        }
        deleteFile(decompressRecord.getSourceFile());
    }

}
