package com.ttgint.parse.operation.handler;

import com.ttgint.library.record.ParseHandlerRecord;
import com.ttgint.library.record.ParseMapRecord;
import com.ttgint.parse.base.ParseXmlHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HwEnbPmXmlParseHandler extends ParseXmlHandler {

    private final Map<String, Long> nodeIds;
    private final HashMap<String, String> headerKeyValue = new HashMap<>();
    private final HashMap<String, String> measInfoKeyValue = new HashMap<>();
    private final HashMap<String, String> indexKey = new HashMap<>();
    private final HashMap<String, String> keyValue = new HashMap<>();
    private String tagValue;
    private String measInfo;
    private String measInfoType;
    private boolean dateSet = false;

    public HwEnbPmXmlParseHandler(ApplicationContext applicationContext,
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
                stringDateFormatter(getHandlerRecord().getFile().getName().split("A")[1].substring(0, 18),
                        "yyyyMMdd.HHmmZ", "yyyy-MM-dd HH:mmZ"));
        headerKeyValue.put("etlApp.constant.nodeName",
                getHandlerRecord().getFile().getName().split("\\_", 2)[1].split("\\.")[0]);
        if (nodeIds.containsKey(headerKeyValue.get("etlApp.constant.nodeName"))) {
            headerKeyValue.put("etlApp.constant.nodeId",
                    String.valueOf(nodeIds.get(headerKeyValue.get("etlApp.constant.nodeName"))));
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tagValue = "";
        switch (qName) {
            case "measCollec":
                if (!dateSet) {
                    headerKeyValue.put("etlApp.constant.measCollec.beginTime",
                            stringDateFormatter(attributes.getValue("beginTime").replace("T", " "),
                                    "yyyy-MM-dd HH:mm:ssXXX", "yyyy-MM-dd HH:mmZ"));
                    dateSet = true;
                }
                break;
            case "measInfo":
                measInfo = attributes.getValue("measInfoId");
                break;
            case "granPeriod":
                measInfoKeyValue.put("etlApp.constant.granPeriod.duration", attributes.getValue("duration"));
                measInfoKeyValue.put("etlApp.constant.granPeriod.endTime",
                        stringDateFormatter(attributes.getValue("endTime").replace("T", " "),
                                "yyyy-MM-dd HH:mm:ssXXX", "yyyy-MM-dd HH:mmZ"));
                break;
            case "measValue":
                keyValue.clear();
                measObjLdnSplitter(attributes.getValue("measObjLdn"));
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
            case "measTypes":
                int keyIndex = 0;
                for (String tagSplit : tagValue.split("\\ ")) {
                    indexKey.put(String.valueOf(keyIndex), tagSplit);
                    keyIndex++;
                }
                break;
            case "measResults":
                int valIndex = 0;
                for (String tagSplit : tagValue.split("\\ ")) {
                    keyValue.put(indexKey.get(String.valueOf(valIndex)).trim(), tagSplit);
                    valIndex++;
                }
                break;
            case "measValue":
                write();
                autoCounterDefine(null, measInfoType, measInfo, keyValue.keySet());
                break;
            case "measInfo":
                keyValue.clear();
                indexKey.clear();
                measInfoKeyValue.clear();
                break;
            case "measCollecFile":
                keyValue.clear();
                indexKey.clear();
                measInfoKeyValue.clear();
                headerKeyValue.clear();
                break;
        }
    }

    @Override
    public void postHandler() {
        keyValue.clear();
        indexKey.clear();
        measInfoKeyValue.clear();
        headerKeyValue.clear();
        nodeIds.clear();
    }

    private void measObjLdnSplitter(String measObjLdn) {
        if (measObjLdn != null) {
            keyValue.put("etlApp.constant.measValue.measObjLdn", measObjLdn);
            measInfoType = measObjLdn.split("\\/", 2)[1].split("\\:", 2)[0];
            Arrays.stream(measObjLdn.replace("/", ",").replace(":", ",").split("\\,"))
                    .filter(value -> value.contains("="))
                    .forEach(value -> {
                        try {
                            keyValue.put("etlApp.measObjLdn." + value.split("\\=", 2)[0].trim(),
                                    value.split("\\=", 2)[1].trim());
                        } catch (Exception e) {
                        }
                    });
        }
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

}
