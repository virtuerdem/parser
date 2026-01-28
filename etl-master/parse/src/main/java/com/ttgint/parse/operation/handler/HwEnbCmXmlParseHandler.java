package com.ttgint.parse.operation.handler;

import com.ttgint.library.record.ParseHandlerRecord;
import com.ttgint.library.record.ParseMapRecord;
import com.ttgint.parse.base.ParseXmlHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HwEnbCmXmlParseHandler extends ParseXmlHandler {

    private final Map<String, Long> nodeIds;
    private final HashMap<String, String> headerKeyValue = new HashMap<>();
    private final HashMap<String, String> measInfoKeyValue = new HashMap<>();
    private final HashMap<String, String> keyValue = new HashMap<>();
    private String measInfo;
    private String tagValue;
    private int moIndex = 0;
    private String attrName;

    public HwEnbCmXmlParseHandler(ApplicationContext applicationContext,
                                  ParseHandlerRecord handlerRecord,
                                  Map<String, Long> nodeIds) {
        super(applicationContext, handlerRecord);
        this.nodeIds = nodeIds;
    }

    @Override
    public void preHandler() {
        if (getHandlerRecord().getFile().getName().contains("^^")) {
            headerKeyValue.put("etlApp.info.fileId", getHandlerRecord().getFile().getName().split("\\^")[0]);
        }
        headerKeyValue.put("etlApp.constant.fragmentDate",
                stringDateFormatter(getHandlerRecord().getFile().getName()
                                .split("_")[getHandlerRecord().getFile().getName().split("_").length - 1]
                                .substring(0, 10) + "+03:00",
                        "yyyyMMddHHXXX", "yyyy-MM-dd HH:mmZ"));
        headerKeyValue.put("etlApp.constant.nodeName", getNodeName(getHandlerRecord().getFile().getName()));
        if (nodeIds.containsKey(headerKeyValue.get("etlApp.constant.nodeName"))) {
            headerKeyValue.put("etlApp.constant.nodeId",
                    nodeIds.get(headerKeyValue.get("etlApp.constant.nodeName")).toString());
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tagValue = "";
        switch (qName) {
            case "MO":
                if (moIndex == 1) {
                    write();
                    autoCounterDefine(null, null, measInfo, keyValue.keySet());
                    clear();
                }
                measInfo = attributes.getValue("className");
                measInfoKeyValue.put("etlApp.constant.MO.fdn", attributes.getValue("fdn"));
                moIndex++;
                break;
            case "attr":
                attrName = attributes.getValue("name");
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
            case "attr":
                measInfoKeyValue.put(attrName, tagValue);
                break;
            case "MO":
                write();
                autoCounterDefine(null, null, measInfo, keyValue.keySet());
                clear();
                break;
        }
    }

    @Override
    public void postHandler() {
        keyValue.clear();
        measInfoKeyValue.clear();
        headerKeyValue.clear();
        nodeIds.clear();
    }

    public String getNodeName(String fileName) {
        return fileName.replace(fileName.split("_")[0] + "_", "")
                .replace("_" + fileName.split("_")[fileName.split("_").length - 2], "")
                .replace("_" + fileName.split("_")[fileName.split("_").length - 1], "");
    }

    private void write() {
        keyValue.putAll(headerKeyValue);
        keyValue.putAll(measInfoKeyValue);
        ParseMapRecord parseMap = getParseMapper().getMapByObjectKey(measInfo);
        if (parseMap != null) {
            keyValue.putAll(prepareUniqueCodes(parseMap, keyValue));
            keyValue.putAll(prepareGeneratedValues(parseMap, keyValue));
            syncWriteIntoFile(parseMap, keyValue);
        } else if (getHandlerRecord().getIsActiveAutoCounter()) {
            keyValue.put("etlApp.info.uniqueRowHashCode", "");
            keyValue.put("etlApp.info.uniqueRowCode", "");
        }
    }

    private void clear() {
        keyValue.clear();
        measInfoKeyValue.clear();
    }

}
