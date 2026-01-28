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
public class HwCsConfXmlParseHandler extends ParseXmlHandler {

    private final Map<String, Long> nodeIds;
    private final HashMap<String, String> headerKeyValue = new HashMap<>();
    private final HashMap<String, String> keyValue = new HashMap<>();
    private String measInfo;
    private int objectIndex = 0;
    private int classIndex = 0;

    public HwCsConfXmlParseHandler(ApplicationContext applicationContext,
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
        switch (qName) {
            case "class":
                classIndex++;
                measInfo = null;
                if (classIndex >= 1) {
                    measInfo = attributes.getValue("name");
                }
                break;
            case "object":
                objectIndex++;
                break;
            case "parameter":
                keyValue.put(attributes.getValue("name"), attributes.getValue("value"));
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case "class":
                classIndex--;
                break;
            case "object":
                objectIndex--;
                if (objectIndex == 1) {
                    write();
                    autoCounterDefine(null, null, measInfo, keyValue.keySet());
                    keyValue.clear();
                }
                break;
            case "parameter":
                break;
        }
    }

    @Override
    public void postHandler() {
        keyValue.clear();
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

}
