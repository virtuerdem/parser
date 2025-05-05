package com.ttgint.library.validation;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

@Slf4j
public class XmlValidation extends Validation {

    public XmlValidation(String sourceFilePath) {
        super(sourceFilePath);
    }

    @Override
    protected void validate() {
        File inputFile = new File(sourceFilePath);
        File tempFile = new File(sourceFilePath + ".tmp");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(tempFile))) {
            String line = null;
            while ((line = reader.readLine()) != null) {
                writer.write((removeInvalidXMLCharacters(line) + "\n").getBytes());
            }
        } catch (Exception exception) {
            log.error("* XmlCharacterCleaner clean sourceFile: {}", inputFile.getName(), exception);
        }
/*
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile, StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(removeInvalidXMLCharacters(line));
                writer.newLine();
            }
        } catch (IOException exception) {
            log.error("Error decompressing source file: {}", decompressRecord.getSourceFilePath(), exception);
        }
*/
        if (!tempFile.renameTo(inputFile)) {
            log.error("* XmlCharacterCleaner rename sourceFile: {}", inputFile.getAbsolutePath());
        }
    }

}
