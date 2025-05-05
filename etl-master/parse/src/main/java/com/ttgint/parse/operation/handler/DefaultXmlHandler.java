package com.ttgint.parse.operation.handler;

import com.ttgint.library.record.ParseHandlerRecord;
import com.ttgint.parse.base.ParseXmlHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

@Slf4j
public class DefaultXmlHandler extends ParseXmlHandler {

    private String tagValue;
    private Integer index;

    public DefaultXmlHandler(ApplicationContext applicationContext, ParseHandlerRecord handlerRecord) {
        super(applicationContext, handlerRecord);
    }

    @Override
    public void preHandler() {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tagValue = "";
        switch (qName) {
            case "fileHeader":
                attributes.getValue("dnPrefix");
                attributes.getValue("fileFormatVersion");
                attributes.getValue("vendorName");
                break;
            case "fileSender":
                attributes.getValue("elementType");
                attributes.getValue("localDn");
                attributes.getValue("elementType");
                break;
            case "measCollec":
                attributes.getValue("beginTime");
                attributes.getValue("endTime");
                break;
            case "managedElement":
                attributes.getValue("localDn");
                attributes.getValue("userLabel");
                break;
            case "measInfo":
                attributes.getValue("measInfoId");
                break;
            case "granPeriod":
                attributes.getValue("duration");
                attributes.getValue("endTime");
                break;
            case "job":
                attributes.getValue("jobId");
                break;
            case "measValue":
                attributes.getValue("measObjLdn");
                break;
            case "measType":
            case "r":
                index = Integer.valueOf(attributes.getValue("p"));
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tagValue += new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case "measType":
                //counterMap.put(tagValue, index);
                break;
            case "r":
                //valueMap.put(index, tagValue);
                break;
            case "measTypes":
                int counterIndex = 0;
                for (String tagSplit : tagValue.split("\\ ")) {
                    //counterMap.put(tagSplit, counterIndex);
                    counterIndex++;
                }
                break;
            case "measResults":
                int valueIndex = 0;
                for (String tagSplit : tagValue.split("\\ ")) {
                    //valueMap.put(valueIndex, tagSplit);
                    valueIndex++;
                }
                break;
            case "measValue":
                //write();
                //clear();
                break;
            case "measInfo":
                //clearAll();
                break;
        }
    }

    @Override
    public void postHandler() {
    }

}
