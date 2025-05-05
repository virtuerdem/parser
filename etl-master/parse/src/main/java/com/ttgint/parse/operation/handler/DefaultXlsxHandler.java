package com.ttgint.parse.operation.handler;

import com.ttgint.library.record.ParseHandlerRecord;
import com.ttgint.parse.base.ParseXlsxHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.springframework.context.ApplicationContext;

@Slf4j
public class DefaultXlsxHandler extends ParseXlsxHandler {

    public DefaultXlsxHandler(ApplicationContext applicationContext, ParseHandlerRecord handlerRecord, Integer sheetIndex) {
        super(applicationContext, handlerRecord, sheetIndex);
    }

    @Override
    public void preHandler() {
    }

    @Override
    public void lineProgress(Long lineIndex, XSSFRow xssfRow) {
        //prepare();
        //write();
        //clear();
    }

    @Override
    public void postHandler() {
    }

}
