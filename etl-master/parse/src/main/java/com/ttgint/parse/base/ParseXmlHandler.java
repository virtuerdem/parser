package com.ttgint.parse.base;

import com.ttgint.library.record.ParseHandlerRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.xml.sax.SAXException;

import javax.xml.parsers.SAXParserFactory;

@Slf4j
public class ParseXmlHandler extends ParseBaseHandler {

    public ParseXmlHandler(ApplicationContext applicationContext, ParseHandlerRecord handlerRecord) {
        super(applicationContext, handlerRecord);
    }

    @Override
    public void preHandler() {
    }

    @Override
    public void onHandler() {
        try {
            SAXParserFactory
                    .newInstance()
                    .newSAXParser()
                    .parse(getHandlerRecord().getFile(), this);
            deleteFile(getHandlerRecord().getProgressType());
        } catch (Exception exception) {
            if (exception instanceof SAXException) {
                if (!exception.getMessage().contains("etlApp.UnknownNewNetworkNode")) {
                    log.error("!saxParseError for {} {} {}", getHandlerRecord().getFile().getName(),
                            exception.toString().split(";", 3)[2], exception.getMessage());
                }
            } else {
                log.error("!saxParseError for {} {}", getHandlerRecord().getFile().getName(), exception.getMessage());
            }
            //deleteFile(ProgressType.PRODUCT);
        }
    }

    @Override
    public void postHandler() {
    }

}
