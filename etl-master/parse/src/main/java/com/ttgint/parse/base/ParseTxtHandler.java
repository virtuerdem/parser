package com.ttgint.parse.base;

import com.ttgint.library.enums.ProgressType;
import com.ttgint.library.record.ParseHandlerRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.BufferedReader;
import java.io.FileReader;

@Slf4j
public abstract class ParseTxtHandler extends ParseBaseHandler {

    public ParseTxtHandler(ApplicationContext applicationContext, ParseHandlerRecord handlerRecord) {
        super(applicationContext, handlerRecord);
    }

    @Override
    public void preHandler() {
    }

    public abstract void lineProgress(Long lineIndex, String line);

    @Override
    public void onHandler() {
        long lineIndex = 0L;
        try (FileReader fileReader
                     = new FileReader(getHandlerRecord().getFile());
             BufferedReader bufferedReader
                     = new BufferedReader(fileReader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lineProgress(lineIndex, line);
                lineIndex++;
            }
            deleteFile(getHandlerRecord().getProgressType());
        } catch (Exception exception) {
            log.error("txt parse error lineNumber: {}", lineIndex, exception);
            deleteFile(ProgressType.PRODUCT);
        }

    }

    @Override
    public void postHandler() {
    }

}