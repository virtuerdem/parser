package com.ttgint.parse.operation.handler;

import com.ttgint.library.record.ParseHandlerRecord;
import com.ttgint.parse.base.ParseCsvHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@Slf4j
public class DefaultCsvHandler extends ParseCsvHandler {

    public DefaultCsvHandler(ApplicationContext applicationContext, ParseHandlerRecord handlerRecord, char splitter) {
        super(applicationContext, handlerRecord, splitter);
    }

    @Override
    public void preHandler() {
    }

    @Override
    public void lineProgress(Long lineIndex, String[] line) {
        //prepare();
        //write();
        //clear();
    }

    @Override
    public void postHandler() {
    }

}
