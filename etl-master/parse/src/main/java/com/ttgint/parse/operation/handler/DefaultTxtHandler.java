package com.ttgint.parse.operation.handler;

import com.ttgint.library.record.ParseHandlerRecord;
import com.ttgint.parse.base.ParseTxtHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

@Slf4j
public class DefaultTxtHandler extends ParseTxtHandler {

    public DefaultTxtHandler(ApplicationContext applicationContext, ParseHandlerRecord handlerRecord) {
        super(applicationContext, handlerRecord);
    }

    @Override
    public void preHandler() {
    }

    @Override
    public void lineProgress(Long lineIndex, String line) {
        //prepare();
        //write();
        //clear();
    }

    @Override
    public void postHandler() {
    }

}
