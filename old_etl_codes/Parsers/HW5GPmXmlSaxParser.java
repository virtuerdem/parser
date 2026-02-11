/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.parserHandler.SaxParserHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.util.HashMap;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author ibrahimegerci
 */
public class HW5GPmXmlSaxParser extends SaxParserHandler {

    private String tagValue;
    private boolean isDate = false;
    private boolean isParse = false;

    private final String neName;
    private final int neRawId;
    private final String dataDate;
    private final int neVendorId;
    private final String topParentColumnName;
    private final String gNodeBId;

    private HashMap<String, String> levelZeroMap = new HashMap<>();
    private HashMap<String, String> levelOneMap = new HashMap<>();
    private HashMap<String, String> levelTwoMap = new HashMap<>();

    public HW5GPmXmlSaxParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType,
            String neName, int neRawId, String dataDate, int vendorId, String topParentColumnName, String gNodeBId) {
        super(currentFileProgress, operationSystem, progType);
        this.neName = neName;
        this.neRawId = neRawId;
        this.dataDate = dataDate;
        this.neVendorId = vendorId;
        this.topParentColumnName = topParentColumnName;
        this.gNodeBId = gNodeBId;
    }

    @Override
    public void onStartParseOperation() {
        levelZeroMap.put("constant.neName", neName);
        levelZeroMap.put("constant.neRawID", Integer.toString(neRawId));
        levelZeroMap.put("constant.dataDate", dataDate);
        levelZeroMap.put("constant.gNodeBId", gNodeBId);
    }

    @Override
    public void onstopParseOperation() {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tagValue = "";
        switch (qName) {
//            case "fileHeader":
//                levelZeroMap.put("constant.fileFormatVersion", attributes.getValue("fileFormatVersion"));
//                break;
//            case "fileSender":
//                levelZeroMap.put("constant.elementType", attributes.getValue("elementType"));
//                break;
            case "measCollec":
                if (!isDate) {
                    levelZeroMap.put("constant.beginTime", CommonLibrary.stringDateFormatter(attributes.getValue("beginTime").replace("T", ".").replace(":", "").replace("-", ""),
                            "yyyyMMdd.HHmmssZ", "yyyyMMddHHmmss"));
                    isDate = true;
                }
                break;
//            case "managedElement":
//                levelZeroMap.put("constant.userLabel", attributes.getValue("userLabel"));
//                break;
            case "measInfo":
                RawTableObject tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetId(attributes.getValue("measInfoId"));
                if (tableObject != null) {
                    levelOneMap.put("constant.measInfoId", attributes.getValue("measInfoId"));
                    isParse = true;
                } else {
                    isParse = false;
                }
                break;
            case "granPeriod":
                if (isParse) {
//                    levelOneMap.put("constant.duration", attributes.getValue("duration"));
                    levelOneMap.put("constant.endTime", CommonLibrary.stringDateFormatter(attributes.getValue("endTime").replace("T", ".").replace(":", "").replace("-", ""),
                            "yyyyMMdd.HHmmssZ", "yyyyMMddHHmmss"));
                }
                break;
            case "measTypes":
                break;
            case "measValue":
                if (isParse) {
                    levelTwoMap.put("constant.measObjLdn", attributes.getValue("measObjLdn"));
                }
                break;
            case "measResults":
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
                if (isParse) {
                    levelOneMap.put("constant.measTypes", tagValue.trim().replace(" ", AbsParserEngine.resultParameter));
                }
                break;
            case "measResults":
                if (isParse) {
                    levelTwoMap.put("constant.measResults", tagValue.trim().replace(" ", AbsParserEngine.resultParameter));
                }
                break;
            case "measValue":
                if (isParse) {
                    levelTwoMap.putAll(levelZeroMap);
                    levelTwoMap.putAll(levelOneMap);
                    writeIntoFile(levelTwoMap);
                    levelTwoMap.clear();
                }
                break;
            case "measInfo":
                isParse = false;
                levelOneMap.clear();
                levelTwoMap.clear();
                break;
        }
    }

    private void writeIntoFile(HashMap<String, String> keysAndValues) {
        try {
            RawTableObject tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetId(keysAndValues.get("constant.measInfoId"));
            if (tableObject != null) {

                String counter = keysAndValues.get("constant.measTypes");
                String value = keysAndValues.get("constant.measResults");
                keysAndValues.remove("constant.measTypes");
                keysAndValues.remove("constant.measResults");

                writeIntoFilesWithController(AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension, //Path
                        CommonLibrary.get_RecordValue(
                                String.join(AbsParserEngine.resultParameter, keysAndValues.keySet()) + AbsParserEngine.resultParameter + counter, //header
                                String.join(AbsParserEngine.resultParameter, keysAndValues.values()) + AbsParserEngine.resultParameter + value, // record
                                tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter), //counterDB
                                "0", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter) + "\n");
            }
        } catch (Exception e) {
            System.err.println("*Parse Error " + e.toString() + " for " + currentFileProgress.getName() + " at " + keysAndValues.get("constant.measInfoId") + " value: " + keysAndValues.get("constant.measObjLdn"));
        }
    }
}
