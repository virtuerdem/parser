/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.parserHandler.SaxParserHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author erdigurbuz
 */
public class ImsXmlPmHandler extends SaxParserHandler {

    private RawTableObject tableObject;
    private String fileContentDataDate;
    private String tableName;
    String fileHeaderNames;
    String tableColumnNames;
    String objectName;
    String record;
    private HashMap<String, String> allActiveFunctionSubsetIdTableName;

    boolean measInfoFlag = false;
    boolean measTypesFlag = false;
    boolean measValueFlag = false;
    boolean measResultsFlag = false;

    public ImsXmlPmHandler(File currentFileProgress, HashMap<String, String> allActiveFunctionSubsetIdTableName, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);

        try {
            String dateOnFile = currentFileProgress.getName().split("\\+")[0];
            dateOnFile = dateOnFile.substring(1, dateOnFile.length());
            this.fileContentDataDate = new SimpleDateFormat("yyyyMMddHHmm").format(new SimpleDateFormat("yyyyMMdd.HHmm").parse(dateOnFile));
            this.allActiveFunctionSubsetIdTableName = allActiveFunctionSubsetIdTableName;
        } catch (Exception ex) {
            dbHelper.insertParserException(ex);
        }
    }

    @Override
    public void onStartParseOperation() {
    }

    @Override
    public void onstopParseOperation() {
        currentFileProgress.delete();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        switch (qName) {
            case "measInfo":
                tableName = null;
                tableName = allActiveFunctionSubsetIdTableName.get(attributes.getValue("measInfoId"));
                if (tableName != null) {
                    measInfoFlag = true;
                    tableObject = TableWatcher.getInstance().getTableObjectFromTableName(tableName);
                    this.tableColumnNames = tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter);
                }
                break;
            case "measTypes":
                fileHeaderNames = "";
                measTypesFlag = true;
                break;
            case "measValue":
                objectName = attributes.getValue("measObjLdn");
                if (objectName.contains("SCSCF")) {
                    objectName = objectName.split("/")[0];
                }
                measValueFlag = true;
                break;
            case "measResults":
                record = "";
                measResultsFlag = true;
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        String tag = new String(ch, start, length);
        if (measTypesFlag) {
            fileHeaderNames += tag.replace(" ", AbsParserEngine.resultParameter);
        }

        if (measResultsFlag) {
            record += tag.replace(" ", AbsParserEngine.resultParameter);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case "measInfo":
                measInfoFlag = false;
                break;
            case "measTypes":
                fileHeaderNames = "DATA_DATE" + AbsParserEngine.resultParameter + "OBJECT_NAME" + AbsParserEngine.resultParameter + fileHeaderNames;
                measTypesFlag = false;
                break;
            case "measResults":
                record = fileContentDataDate + AbsParserEngine.resultParameter + objectName + AbsParserEngine.resultParameter + record;
                measResultsFlag = false;
                //Tablo bilgileri hazir yazmaya
                if (tableName != null) {
                    try {
                        String fullPath = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;
                        record = record.replace("|NIL|", "|0|").replace("|NIL", "|0").replace("NIL|", "0|");
                        writeIntoFilesWithController(fullPath, CommonLibrary.get_RecordValue(fileHeaderNames, record, tableColumnNames, "0", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter) + "\n");
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                }
                break;
        }

    }

}
