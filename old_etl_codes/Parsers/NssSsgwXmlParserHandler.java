/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_xml_NssSsgw.neNametoIdList;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import com.ttgint.parserEngine.common.RawCounterObject;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ParserIOException;
import com.ttgint.parserEngine.parserHandler.SaxParserHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author erdigurbuz
 */
public class NssSsgwXmlParserHandler extends SaxParserHandler {

    private String currentTagValue;
    private String currentDate;
    private String measTypes;
    private Date date;
    private NssSsgwSubset subset;
    private String unieqeName;
    private HashMap<String, Boolean> notExistNe = new HashMap<>();
    RawTableObject tableObject;

    public final SimpleDateFormat dateFormatterFromFile = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public final SimpleDateFormat dateFormattertoResult = new SimpleDateFormat("yyyyMMddHHmm");

    Boolean measInfoFlag = false;
    Boolean granPeriodFlag = false;
    Boolean measTypesFlag = false;
    Boolean measValueFlag = false;
    Boolean measResultsFlag = false;
    Boolean suspectFlag = false;

    @Override
    public void onStartParseOperation() {
        String fileName = currentFileProgress.getName();
        String splittedFilename[] = fileName.split("\\_");
        unieqeName = splittedFilename[splittedFilename.length - 1];
    }

    public NssSsgwXmlParserHandler(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentTagValue = "";
        
        switch (qName) {
            case "measInfo":
                measInfoFlag = true;
                break;
            case "granPeriod":
                granPeriodFlag = true;
                currentDate = attributes.getValue("endTime");
                //"2014-12-23T03:16:21
                currentDate = currentDate.split("\\.")[0].replace("T", " ");
                String splittedDate[] = currentDate.split("\\:");
                currentDate = splittedDate[0] + ":" + parseDate(splittedDate[1]);
                 {
                    try {
                        date = dateFormatterFromFile.parse(currentDate);
                    } catch (ParseException ex) {
                    }
                }

                subset = new NssSsgwSubset();
                subset.setOssNo(2);

                break;
            case "measTypes":
                measTypesFlag = true;
                break;
            case "measValue":
                subset.setFunctionSubset(attributes.getValue("measObjLdn").split("=")[0]);
                tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(attributes.getValue("measObjLdn").split("=")[0]);
                subset.setMeasObjLdn(attributes.getValue("measObjLdn").replaceAll("&quot;", "\""));
                measValueFlag = true;
                break;
            case "measResults":
                measResultsFlag = true;
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        currentTagValue += new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case "measInfo":
                subset = null;
                measInfoFlag = false;
                break;
            case "granPeriod":
                granPeriodFlag = false;
                break;
            case "measTypes":
                measTypes = currentTagValue;
                measTypesFlag = true;
                break;
            case "measValue":
                measValueFlag = false;
                break;
            case "measResults":
                if (tableObject != null) {
                    //Constant Header
                    if (measTypesFlag) {
                        for (RawCounterObject each : tableObject.getConstantObjectList()) {
                            subset.addHeader(each.getCounterNameFile());
                        }
                    }

                    //Constant Values
                    subset.addValue(subset.generateId("99"));
                    subset.addValue(unieqeName);
                    subset.addValue(currentDate);
                    if (neNametoIdList.get(unieqeName) == null) {
                        notExistNe.put(unieqeName, true);
                        break;
                    }
                    String constantExtraColumns = subset.getMeasObjLdn();
                    subset.addValue(constantExtraColumns);
                    //Network id üretilecek ise
                    if (subset.getVariableFullHeader().contains("NETWORK_ID")) {
                        subset.addValue(subset.generateId(neNametoIdList.get(unieqeName)));
                    }

                    if (subset.getVariableFullHeader().split("\\" + AbsParserEngine.resultParameter).length
                            != subset.getVariableFullValues().split("\\" + AbsParserEngine.resultParameter).length) {
                        for (String value : constantExtraColumns.split("=")[1].split(",")) {
                            subset.addValue(value.trim());
                        }
                    }

                    if (measTypesFlag) {
                        //Olmayan column degeri var ise bosluk doldur
                        if (subset.getVariableFullHeader().split("\\" + AbsParserEngine.resultParameter).length
                                != subset.getVariableFullValues().split("\\" + AbsParserEngine.resultParameter).length) {
                            for (int i = 0; i < subset.getVariableFullHeader().split("\\" + AbsParserEngine.resultParameter).length
                                    - subset.getVariableFullValues().split("\\" + AbsParserEngine.resultParameter).length; i++) {
                                subset.addValue("");
                            }
                        }

                        //Variable Headers
                        for (String each : measTypes.split(" ")) {
                            subset.addHeader(each);
                        }
                        measTypesFlag = false;
                    }

                    //Variable Values
                    for (String each : currentTagValue.split(" ")) {
                        subset.addValue(each);
                    }

                    readyToWrite(subset);
                    subset.resetValues();
                }
                measResultsFlag = false;
                break;

            default:

                break;
        }
    }

    private void readyToWrite(NssSsgwSubset sub) {

        if (tableObject instanceof RawTableObject) {

            String Record;
            Record = CommonLibrary.get_RecordValue(sub.getVariableFullHeader(), sub.getVariableFullValues(), tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter),
                    "0", "|", "|");
            if (Record.contains("EXTRA")) {
                Record = Record.replace("EXTRA", "");
            }
            Record += "\n";

            if (Record.contains("DONTWRITE") == false) {
                String fileName = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;

                try {
                    writeIntoFilesWithController(fileName, Record);
                } catch (ParserIOException ex) {

                }

            }

        }
    }

    static String parseDate(String minute) {
        int min = Integer.parseInt(minute);
        int result = min / 15;
        String lll = null;
        switch (result) {
            case 0:
                lll = "00";
                break;
            case 1:
                lll = "15";
                break;
            case 2:
                lll = "30";
                break;
            case 3:
                lll = "45";
                break;

        }
        return lll;

    }

    @Override
    public void onstopParseOperation() {
        String notExist = "";
        for (Object k : notExistNe.keySet()) {
            notExist += k.toString() + ",";
        }
        if (!notExist.isEmpty()) {
            notExist = "Not exist: " + notExist.substring(0, notExist.length() - 1);
            dbHelper.insertParserException(notExist);
        }

    }

}
