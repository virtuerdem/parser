package com.ttgint.library.decompress;

import com.ttgint.library.record.DecompressRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
public class Asn1Decompress extends Decompress {

    public Asn1Decompress(ApplicationContext applicationContext, DecompressRecord decompressRecord) {
        super(applicationContext, decompressRecord);
    }

    @Override
    protected List<File> decompress() {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Process proc = new ProcessBuilder(
                    ("perl Asn1Decoder.pl " + decompressRecord.getSourceFile().getAbsolutePath()).split(" "))
                    .start();

            try (InputStreamReader inputStreamReader = new InputStreamReader(proc.getInputStream());
                 BufferedReader reader = new BufferedReader(inputStreamReader)) {
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

            Arrays.stream(Objects.requireNonNull(new File(decompressRecord.getSourceFile().getParent()).listFiles()))
                    .filter(e -> e.isFile()
                            && !e.getName().equals(".")
                            && !e.getName().equals("..")
                            && e.getName().endsWith(".csv")
                            && e.getName().startsWith(decompressRecord.getSourceFile().getName().replace(".asn1", "")))
                    .forEach(e -> {
                        fileList.add(e);
                        this.insertResult(e);
                    });
        } catch (Exception exception) {
            insertError("ASN1001", null, stringBuilder.toString());
            Arrays.stream(Objects.requireNonNull(new File(decompressRecord.getSourceFile().getParent()).listFiles()))
                    .filter(e -> e.isFile()
                            && !e.getName().equals(".")
                            && !e.getName().equals("..")
                            && !e.getName().contains(".asn1")
                            && e.getName().startsWith(decompressRecord.getSourceFile().getName().replace(".asn1", "")))
                    .forEach(this::deleteFile);
        }
        deleteFile(decompressRecord.getSourceFile());
        return fileList;
    }

}
