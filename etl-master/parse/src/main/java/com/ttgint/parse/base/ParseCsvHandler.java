package com.ttgint.parse.base;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.ttgint.library.enums.ProgressType;
import com.ttgint.library.record.ParseHandlerRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.BufferedReader;
import java.io.FileReader;

@Slf4j
public abstract class ParseCsvHandler extends ParseBaseHandler {

    private char splitter;

    public ParseCsvHandler(ApplicationContext applicationContext, ParseHandlerRecord handlerRecord, char splitter) {
        super(applicationContext, handlerRecord);
        this.splitter = splitter;
    }

    @Override
    public void preHandler() {
    }

    public abstract void lineProgress(Long lineIndex, String[] line);

    @Override
    public void onHandler() {
        long lineIndex = 0L;
        try (FileReader fileReader
                     = new FileReader(getHandlerRecord().getFile());
             BufferedReader bufferedReader
                     = new BufferedReader(fileReader);
             CSVReader csvReader
                     = new CSVReaderBuilder(bufferedReader)
                     .withCSVParser(new CSVParserBuilder().withSeparator(splitter).build()).build()) {
            for (String[] strings : csvReader) {
                lineProgress(lineIndex, strings);
                lineIndex++;
            }
            deleteFile(getHandlerRecord().getProgressType());
        } catch (Exception exception) {
            log.error("csv parse error lineNumber: {}", lineIndex, exception);
            deleteFile(ProgressType.PRODUCT);
        }
    }

    @Override
    public void postHandler() {
    }

}
