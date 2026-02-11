/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_Ncore.flag;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.parserHandler.SaxParserHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_Ncore.fileCounters;
import com.ttgint.parserEngine.exceptions.ParserIOException;

/**
 *
 * @author enesmalik.terzi, erdi.gurbuz
 */
public class NcoreXmlParser extends SaxParserHandler {

    public NcoreXmlParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
        //dataDate = currentFileProgress.getName().split("\\-")[1].substring(2, 14);
    }

    @Override
    public void onStartParseOperation() {
    }

    public String ipAdress;
    private String ossNo;
    private NcoreSubset currentSubset;
    private String currentTagValue;
    private boolean isCounterStarted = false;
    private boolean isCounterValueStarted = false;
    String counterstr;
    String valuestr;
    String dateFromFile;
    boolean isDateSetted = false;
    //private String dataDate = "";
    private String dataBeginTime = "";
    private String elementType = "";

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentTagValue = "";

        switch (qName) {
            case "fileSender":
                elementType = attributes.getValue("elementType");
                break;
            case "measCollec":
                if (!isDateSetted) {
                    dataBeginTime = attributes.getValue("beginTime").split("\\+")[0].replace("T", "").replace("-", "").replace(":", "").substring(0, 12);
                    isDateSetted = true;
                }
                break;
            case "granPeriod":
                String period = attributes.getValue("duration");
                dateFromFile = attributes.getValue("endTime");
                String fixedDate = parserDate(dateFromFile, period);
                fixedDate = fixDate(fixedDate, period);
                currentSubset.setDateAsString(fixedDate);
                currentSubset.createProperty();
                currentSubset.addProperty("DATA_DATE", currentSubset.getDateAsString());
                if (ossNo == null) {
                    currentSubset.addProperty("OSSNO", "ossn1");
                }
                currentSubset.addProperty("PERIOD_START_TIME", currentSubset.getDateAsString());

                currentSubset.addProperty("DATA_BEGIN_TIME", dataBeginTime);
                currentSubset.addProperty("DATA_DURATION", attributes.getValue("duration"));
                currentSubset.addProperty("DATA_END_TIME", attributes.getValue("endTime").split("\\+")[0].replace("T", "").replace("-", "").replace(":", "").substring(0, 12));

                break;
            case "measInfo":
                currentSubset = new NcoreSubset();
                currentSubset.setMeasType(attributes.getValue("measInfoId"));
                break;
            case "measTypes":
                isCounterStarted = true;
                break;
            case "measResults":
                isCounterValueStarted = true;
                break;
            case "measValue":
                String dnValue = attributes.getValue("measObjLdn");
                currentSubset.addObject(dnValue);

                if (dnValue.contains("-PLMN/") || dnValue.contains("-Netact/")) {
                    try {
                        String[] arr = dnValue.split("/");
                        String[] arr2 = Arrays.copyOfRange(arr, 1, arr.length);
                        dnValue = CommonLibrary.joinString(arr2, "/");
                    } catch (Exception ex) {
                        dnValue = (dnValue.split("-Netact/")[1]);
                    }
                    currentSubset.setMeasObjLdn(dnValue);
                }

                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        currentTagValue += new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (isCounterStarted) {
            counterstr = currentTagValue;
            isCounterStarted = false;
        }
        if (isCounterValueStarted) {
            valuestr = currentTagValue;
            isCounterValueStarted = false;
            String[] counterlist = counterstr.split(" ");
            String[] valuelist = valuestr.split(" ");
            for (int i = 0; i < counterlist.length; i++) {
                currentSubset.addProperty(counterlist[i].trim(), valuelist[i].trim());
            }
            isCounterValueStarted = false;
        }

        switch (qName) {

            case "measValue":
                isCounterStarted = false;
                break;
            case "measInfo":
                finalTouch();
                if (flag) {
                    autoCounter();
                }
                break;
            case "measCollecFile":
                currentSubset = null;
                break;
        }
    }

    @Override
    public void onstopParseOperation() {

    }

    private void finalTouch() {
        String fileFunctionName = currentFileProgress.getName().split("\\+")[1].split("48")[1].split("\\.|\\_")[0];
        RawTableObject tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetNameAndNeTypeByContains(currentSubset.getMeasType(), fileFunctionName);

        if (tableObject != null) {
            String myColumns = tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter);
            String fullHeader = currentSubset.getFullHeader();
            String fullValue = currentSubset.getFullValues();

            String line = CommonLibrary.get_RecordValue(fullHeader, fullValue, myColumns, "0", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter);
            String tableName = tableObject.getTableName();

            try {
                String fullFilePath = AbsParserEngine.LOCALFILEPATH + tableName + AbsParserEngine.integratedFileExtension;
                writeIntoFilesWithController(fullFilePath, line + "\n");
            } catch (ParserIOException ex) {
            }
        }
    }

    private void autoCounter() {
        for (String rawCounter : currentSubset.getFullAutoCounters()) {
            switch (rawCounter) {
                case "PERIOD_START_TIME":
                case "DATA_DATE":
                case "OSSNO":
                case "NETWORK_ID":
                case "OBJECT_NAME":
                case "DATA_BEGIN_TIME":
                case "DATA_DURATION":
                case "DATA_END_TIME":
                case "":
                    continue;
            }
            addConcurrent(elementType + "|" + currentSubset.getMeasType() + "|" + rawCounter);
        }
    }

    private static synchronized void addConcurrent(String allCounters) {
        fileCounters.put(allCounters, "");
    }

    private static String fixDate(String date, String period) {
        date = date.split("\\+")[0];
        Date f;
        try {
            f = new SimpleDateFormat("yyyyMMddHHmm").parse(date);
        } catch (ParseException ex) {
            ex.printStackTrace();
            return "";
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(f);
        int duration = Integer.valueOf(period.split("PT")[1].replace("S", ""));

        switch (duration) {
            case 86400:
                cal.add(Calendar.DATE, -1);
                break;
            case 3600:
                cal.add(Calendar.HOUR, -1);
                break;
            case 1800:
                cal.add(Calendar.MINUTE, -30);
                break;
            case 900:
                cal.add(Calendar.MINUTE, -15);
                break;
            case 300:
                cal.add(Calendar.MINUTE, -5);
                break;
            default:

        }

        return new SimpleDateFormat("yyyyMMddHHmm").format(cal.getTime());
    }

    private String parserDate(String date, String period) {
        String dateStr = date.substring(0, 16);
        String dataDateWithoutHour = (dateStr.split(":")[0]).replace("T", "").replace("-", "");
        String dataDateHour = "00";
        //saniye gelen period dk çevriliyor.
        int spliterPeriod = Integer.valueOf(period.split("PT")[1].replace("S", "")) / 60;
        //int 
        if (spliterPeriod == 5 || spliterPeriod == 10 || spliterPeriod == 15) {
            int ddHour = Integer.valueOf(dateStr.split(":")[1]);
            dataDateHour = null;
            switch (ddHour / 15) {
                case 0:
                    dataDateHour = "00";
                    break;
                case 1:
                    dataDateHour = "15";
                    break;
                case 2:
                    dataDateHour = "30";
                    break;
                case 3:
                    dataDateHour = "45";
                    break;
            }
        } else if (spliterPeriod == 30) {
            int ddHour = Integer.valueOf(dateStr.split(":")[1]);
            dataDateHour = null;
            switch (ddHour / 30) {
                case 0:
                    dataDateHour = "00";
                    break;
                case 1:
                    dataDateHour = "30";
                    break;
            }
        }

        return (dataDateWithoutHour + dataDateHour);
    }
}
