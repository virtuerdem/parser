
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_FemtoBSR;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.RawCounterObject;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ParserIOException;
import com.ttgint.parserEngine.parserHandler.SaxParserHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author erdigurbuz
 */
public class FemtoXmlPmHandler extends SaxParserHandler {

    private TableWatcher tableWtchrObj = TableWatcher.getInstance();

    public FemtoXmlPmHandler(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progressType) {
        super(currentFileProgress, operationSystem, progressType);
    }

    private String tag;
    private String lastMeasType;
    private String clusterId;
    private String cellId;
    private String neName;
    private String hnbId;
    private boolean flag_fileDate = false;
    private boolean flag_grandPeriod = false;
    private boolean flag_measName = false;
    private boolean flag_measType = false;
    private boolean flag_measValue = false;
    private boolean flag_nedn = false;
    private boolean flag_neun = false;

    private boolean flagParentId = true;
    private boolean flagNetworkId = true;

    private Date fileDate_date;
    private String fileDate_Str;
    private String fileGrandPeriod_Str;

    private String tableColumnNames;
    private String fileHeaderNames = "";

    private final SimpleDateFormat fileDateFormatter = new SimpleDateFormat("yyyyMMddHHmm");
    private FemtoFunctionSubset subSet = null;

    private void setTableColumnNames(String rawTableName) throws IOException {
        RawTableObject tableObject = tableWtchrObj.getTableObjectFromTableName(rawTableName);
        this.tableColumnNames = tableObject.getFullColumnOrderUsingCounterDbName(AbsParserEngine.resultParameter);
    }

    @Override
    public void onStartParseOperation() {
    }

    @Override
    public void onstopParseOperation() {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

        switch (qName) {
            case "cbt":
                flag_fileDate = true;
                break;
            case "neun":
                flag_neun = true;
                break;
            case "nedn":
                flag_nedn = true;
                break;
            case "gp":
                flag_grandPeriod = true;
                break;
            case "mn":
                flag_measName = true;

                break;
            case "mt":
                flag_measType = true;
                break;
            case "mv":
                flag_measValue = true;
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tag = new String(ch, start, length);

        if (flag_measName) {
            String measName = tag.trim();
            subSet = new FemtoFunctionSubset();
            subSet.setMeasName(measName);
            flag_measName = false;
        }

        if (flag_measType) {
            String measType = tag.trim();
            lastMeasType = measType;
            subSet.addMeasType(measType);
            flag_measType = false;
        }

        if (flag_measValue) {
            String measValue = tag.trim();
            subSet.addMeasValue(lastMeasType, measValue);
            flag_measValue = false;
        }
    }

    private String generateNetworkId(int vendorId, int neTypeId, int _clusterId, int _cellId) {
        BigDecimal bd = new BigDecimal("10");
        BigDecimal resultBigDecimal = bd.pow(21).multiply(BigDecimal.valueOf(vendorId)).
                add(bd.pow(16).multiply(BigDecimal.valueOf(neTypeId))).
                add(bd.pow(8).multiply(BigDecimal.valueOf(_clusterId))).
                add(BigDecimal.valueOf(_cellId));

        return resultBigDecimal.toString();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String record = "";

        //File tarih-saati
        if (flag_fileDate) {
            fileDate_Str = tag;
            String[] splittedDate = fileDate_Str.split("\\+");
            fileDate_Str = splittedDate[0];
            fileDate_Str = fileDate_Str.substring(0, fileDate_Str.length() - 2);

            try {
                fileDate_date = fileDateFormatter.parse(fileDate_Str);
            } catch (ParseException ex) {
                AbsParserEngine.dbHelper.insertParserException(ex);
            }
            flag_fileDate = false;
        }

        if (flag_grandPeriod) {
            fileGrandPeriod_Str = (Integer.parseInt(tag) / 60) + "";
            flag_grandPeriod = false;
        }

        if (flag_neun) {
            String[] splittedNeun = tag.split(",");

            for (String each : splittedNeun) {

                if (each.contains("bSRName")) {
                    neName = each.replace("bSRName=", "");
                }

                if (each.contains("HNBId")) {
                    hnbId = each.replace("HNBId=", "");
                }

            }
            flag_neun = false;
        }

        if (flag_nedn) {
            String[] splittedNeun = tag.split(",");

            for (String each : splittedNeun) {

                if (each.contains("SubNetwork")) {
                    clusterId = each.replace("SubNetwork=", "");
                }

                if (each.contains("ManagedElement")) {
                    cellId = each.replace("ManagedElement=", "");
                }
            }
            flag_nedn = false;
        }

        //Tablo icin result file olusturma
        if ("mn".equals(qName)) {
            String tableName = "";
            String functionSubsetName = subSet.getMeasName();
            try {

                tableName = tableWtchrObj.getTableObjectFromFunctionSubsetName(functionSubsetName).getTableName();

            } catch (Exception ex) {
            }
            Boolean flagParse = false;
            if (!tableName.isEmpty()) {
                flagParse = true;
            }

            //Tablo aktif ise parse et
            if (flagParse) {
                //Object icin id generate
                String topParentId = generateNetworkId(29, 31, 1, 1);
                String parentId = generateNetworkId(29, 30, 1, 375);
                String neId = "";
                if (flagParentId) {
                    ParserEngine_pm_FemtoBSR.parentMap.put("topParentId", topParentId + "|" + topParentId + "|" + "FGW1" + "|" + "31" + "|" + fileDate_Str + "|" + topParentId + "|");
                    ParserEngine_pm_FemtoBSR.parentMap.put("parentId", parentId + "|" + topParentId + "|" + "CI375" + "|" + "30" + "|" + fileDate_Str + "|" + topParentId + "|");
                    flagParentId = false;
                }

                try {
                    setTableColumnNames(tableName);
                } catch (IOException ex) {
                    // dbHelper.insertParserException(ex);
                }

                try {
                    String fullPath = AbsParserEngine.LOCALFILEPATH + tableName + AbsParserEngine.integratedFileExtension;

                    record = fileDate_Str + AbsParserEngine.resultParameter + fileGrandPeriod_Str + AbsParserEngine.resultParameter;
                    //NetworkID
                    neId = generateNetworkId(29, 37, Integer.parseInt(clusterId), Integer.parseInt(cellId));
                    record += neId + AbsParserEngine.resultParameter;

                    if (flagNetworkId) {
                        String row = neId + "|" + parentId + "|" + neName + "|" + "37" + "|" + fileDate_Str + "|" + topParentId + "|" + hnbId + "\n";
                        ParserEngine_pm_FemtoBSR.objects.put(neId, row);
                        flagNetworkId = false;
                    }

                    HashMap<String, String> counterList = subSet.returnCounterTypeAndValue();
                    fileHeaderNames = "DATA_DATE" + AbsParserEngine.resultParameter + "GRANULARITY" + AbsParserEngine.resultParameter
                            + "NETWORK_ID" + AbsParserEngine.resultParameter;

                    for (Map.Entry<String, String> entry : counterList.entrySet()) {
                        String counterName = entry.getKey();
                        String counterValue = entry.getValue();

                        if (!counterName.isEmpty() && !counterName.equals("")) {

                            RawTableObject rawCounterObject = tableWtchrObj.getTableObjectFromCounterName(new ArrayList<String>(counterList.keySet()));

                            if (rawCounterObject != null) {

                                ArrayList<RawCounterObject> list = rawCounterObject.getCounterObjectList();

                                for (RawCounterObject list1 : list) {

                                    if (list1.getCounterNameFile() != null && list1.getCounterNameFile().equals(counterName)) {

                                        fileHeaderNames += list1.getCounterNameDb() + AbsParserEngine.resultParameter;

                                        record += counterValue + AbsParserEngine.resultParameter;

                                    }
                                }
                            }
                        }
                    }

                    fileHeaderNames = fileHeaderNames.substring(0, fileHeaderNames.length() - 1);

                    record = record.substring(0, record.length() - 1);

                    record = CommonLibrary.replaceNullValuesWithZero(record);

                    record = CommonLibrary.get_RecordValue(fileHeaderNames, record, tableColumnNames, "0", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter) + "\n";

                    writeIntoFilesWithController(fullPath, record);
                } catch (NumberFormatException ex) {
                    AbsParserEngine.dbHelper.insertParserException(ex);
                } catch (ParserIOException ex) {
                    AbsParserEngine.dbHelper.insertParserException(ex);
                }
            }
        }
    }
}
