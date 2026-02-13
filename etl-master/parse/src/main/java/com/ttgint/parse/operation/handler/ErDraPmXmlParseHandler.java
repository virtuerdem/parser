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
public class ErDraPmXmlParseHandler extends ParseXmlHandler {

    private final Map<String, Long> nodeIds;
    private final HashMap<String, String> headerKeyValue = new HashMap<>();
    private final HashMap<String, String> measInfoKeyValue = new HashMap<>();
    private final HashMap<String, String> indexKey = new HashMap<>();
    private final HashMap<String, String> keyValue = new HashMap<>();
    private String tagValue;
    private String index;
    private String measInfo;
    private boolean dateSet = false;

    public ErDraPmXmlParseHandler(ApplicationContext applicationContext,
                                  ParseHandlerRecord handlerRecord,
                                  Map<String, Long> nodeIds) {
        super(applicationContext, handlerRecord);
        this.nodeIds = nodeIds;
    }

    @Override
    public void preHandler() {
        if (getHandlerRecord().getFile().getName().contains("^^")) {
            headerKeyValue.put("etlApp.info_fileId", getHandlerRecord().getFile().getName().split("\\^")[0]);
        }
        headerKeyValue.put("etlApp.constant_fragmentDate",
                stringDateFormatter(getHandlerRecord().getFile().getName().split("A")[1].substring(0, 18),
                        "yyyyMMdd.HHmmZ", "yyyy-MM-dd HH:mmZ"));
        headerKeyValue.put("etlApp.constant_nodeName",
                "dra" + getHandlerRecord().getFile().getName().split("=dra")[1].split("\\,")[0]);
        if (nodeIds.containsKey(headerKeyValue.get("etlApp.constant_nodeName"))) {
            headerKeyValue.put("etlApp.constant_nodeId",
                    nodeIds.get(headerKeyValue.get("etlApp.constant_nodeName")).toString());
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tagValue = "";
        switch (qName) {
//            case "fileHeader":
//                headerKeyValue.put("etlApp.constant.fileHeader_fileFormatVersion", attributes.getValue("fileFormatVersion"));
//                headerKeyValue.put("etlApp.constant.fileHeader_dnPrefix", attributes.getValue("dnPrefix"));
//                headerKeyValue.put("etlApp.constant.fileHeader_vendorName", attributes.getValue("vendorName"));
//                break;
//            case "fileSender":
//                headerKeyValue.put("etlApp.constant.fileSender_elementType", attributes.getValue("elementType"));
//                headerKeyValue.put("etlApp.constant.fileSender_localDn", attributes.getValue("localDn"));
//                break;
            case "measCollec":
                if (!dateSet) {
                    headerKeyValue.put("etlApp.constant.measCollec_beginTime",
                            stringDateFormatter(attributes.getValue("beginTime").replace("T", " "),
                                    "yyyy-MM-dd HH:mm:ssXXX", "yyyy-MM-dd HH:mmZ"));
                    dateSet = true;
//                } else {
//                    headerKeyValue.put("etlApp.constant.measCollec_endTime", attributes.getValue("endTime"));
                }
                break;
//            case "managedElement":
//                headerKeyValue.put("etlApp.constant.managedElement_localDn", attributes.getValue("localDn"));
//                headerKeyValue.put("etlApp.constant.managedElement_userLabel", attributes.getValue("userLabel"));
//                headerKeyValue.put("etlApp.constant.managedElement_swVersion", attributes.getValue("swVersion"));
//                break;
            case "measInfo":
                measInfo = attributes.getValue("measInfoId");
                break;
//            case "job":
//                measInfoKeyValue.put("etlApp.constant.job_jobId", attributes.getValue("jobId"));
//                break;
            case "granPeriod":
                measInfoKeyValue.put("etlApp.constant.granPeriod_duration", attributes.getValue("duration"));
                measInfoKeyValue.put("etlApp.constant.granPeriod_endTime",
                        stringDateFormatter(attributes.getValue("endTime").replace("T", " "),
                                "yyyy-MM-dd HH:mm:ssXXX", "yyyy-MM-dd HH:mmZ"));
                break;
//            case "repPeriod":
//                measInfoKeyValue.put("etlApp.constant.repPeriod_duration", attributes.getValue("duration"));
//                break;
            case "measValue":
                keyValue.clear();
                measObjLdnSplitter(attributes.getValue("measObjLdn"));
                break;
            case "measType":
            case "r":
                index = attributes.getValue("p");
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
                indexKey.put(index, tagValue);
                break;
            case "r":
                keyValue.put(indexKey.get(index).trim(), tagValue);
                break;
            case "measValue":
                write();
                autoCounterDefine(null, null, measInfo, keyValue.keySet());
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
            keyValue.put("etlApp.constant.measValue_measObjLdn", measObjLdn);
            Arrays.stream(measObjLdn.split("\\,"))
                    .forEach(value -> {
                        try {
                            keyValue.put("etlApp.measObjLdn_" + value.split("\\=", 2)[0],
                                    value.split("\\=", 2)[1]);
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
            keyValue.put("etlApp.info_uniqueRowHashCode", "");
            keyValue.put("etlApp.info_uniqueRowCode", "");
        }
    }

}
