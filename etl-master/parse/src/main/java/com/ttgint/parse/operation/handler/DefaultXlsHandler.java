package com.ttgint.parse.operation.handler;

import com.ttgint.library.record.ParseHandlerRecord;
import com.ttgint.parse.base.ParseXlsHandler;
import jxl.Cell;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@Slf4j
public class DefaultXlsHandler extends ParseXlsHandler {

    public DefaultXlsHandler(ApplicationContext applicationContext, ParseHandlerRecord handlerRecord, Integer sheetIndex) {
        super(applicationContext, handlerRecord, sheetIndex);
    }

    @Override
    public void preHandler() {
    }

    @Override
    public void lineProgress(Long lineIndex, Cell[] cell) {
        //prepare();
        //write();
        //clear();
    }

    @Override
    public void postHandler() {
    }

}
