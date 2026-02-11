/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_Huawei;
import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_Huawei.neNameAndIdList3G;
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
public class HW4GPmXmlSaxParser extends SaxParserHandler {

    private String tagValue;

    private boolean isParse = false;
//    private boolean isSuspect = false;

    private final String neName;
    private final int neRawId;
    private final String dataDate;
    private final int vendorId;

    private String measInfo = "";
    private String measTypes = "";
    private String measResults = "";
    private String measObjLdn = "";
    private StringBuilder sb;
    private RawTableObject tableObject;
    private String writePath = "";

    public HW4GPmXmlSaxParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType,
            String neName, int neRawId, String dataDate, int vendorId) {
        super(currentFileProgress, operationSystem, progType);
        this.neName = neName;
        this.neRawId = neRawId;
        this.dataDate = dataDate;
        this.vendorId = vendorId;
    }

    @Override
    public void onStartParseOperation() {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tagValue = "";
        switch (qName) {
            case "measInfo":
                measInfo = attributes.getValue("measInfoId");
                tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetId(measInfo);
                if (tableObject != null) {
                    sb = new StringBuilder();
                    writePath = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;
                    isParse = true;
                } else {
                    isParse = false;
                }
                break;
            case "measValue":
                if (isParse) {
                    measObjLdn = attributes.getValue("measObjLdn");
                }
                break;
//            case "suspect":
//                isSuspect = true;
//                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tagValue += new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (isParse) {
            switch (qName) {
                case "measTypes":
                    measTypes = tagValue.trim()
                            .replace(" ", AbsParserEngine.resultParameter);
                    break;
                case "measResults":
                    measResults = tagValue.trim()
                            .replace(" ", AbsParserEngine.resultParameter)
                            .replace("NIL", "0");
                    break;
                case "measValue":
                    prepareRawData();
                    measResults = "";
                    measObjLdn = "";
                    break;
                case "measInfo":
                    writeIntoFile();
                    isParse = false;
                    measInfo = "";
                    measTypes = "";
                    measResults = "";
                    measObjLdn = "";
                    sb = new StringBuilder();
                    tableObject = null;
                    writePath = "";
                    break;
            }
        }
    }

    @Override
    public void onstopParseOperation() {
    }

    private void writeIntoFile() {
        try {
            if (sb.length() > 1) {
                writeIntoFilesWithController(writePath, sb.toString());
            }
        } catch (Exception e) {
            System.err.println("*ParseWrite Error " + e.toString() + " for " + currentFileProgress.getName() + " at " + measInfo);
        }
    }

    private void prepareRawData() {
        try {
            HashMap<String, String> constantKeysValues
                    = prepareConstantValues(tableObject.getTableType() + "-" + tableObject.getNetworkIdType(), measObjLdn);

            if (constantKeysValues.size() > 1) {
                StringBuilder sbHeader = new StringBuilder(measTypes);
                StringBuilder sbRecord = new StringBuilder(measResults);

                constantKeysValues.put("DATA_DATE", dataDate);
                constantKeysValues.keySet()
                        .forEach(e -> {
                            sbHeader.append(AbsParserEngine.resultParameter).append(e);
                            sbRecord.append(AbsParserEngine.resultParameter).append(constantKeysValues.get(e));
                        });

                sb.append(CommonLibrary.get_RecordValue(
                        sbHeader.toString(), //header
                        sbRecord.toString(), // record
                        tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter), //counterDB
                        "0",
                        AbsParserEngine.resultParameter,
                        AbsParserEngine.resultParameter))
                        .append("\n");
            }
        } catch (Exception e) {
            System.err.println("*Parse Error " + e.getMessage()
                    + " for " + currentFileProgress.getName()
                    + " at " + measInfo
                    + " of " + tableObject.getTableType() + "-" + tableObject.getNetworkIdType()
                    + " value: " + measObjLdn);
        }
    }

    public HashMap<String, String> prepareConstantValues(String dataType, String measObj) {
        HashMap<String, String> measObjList = CommonLibrary.getMeasObjLdnWithType(measObj);
        HashMap<String, String> keysValues = new HashMap<>();
//        measObjList.keySet().forEach(e -> keysValues.put("measObjLdn." + e, measObjList.get(e)));
        String nodebName = "";
        String rncNeId = null;
        boolean isMissing = false;
        switch (dataType) {
            // generating measObjLdn string completely as NETWORK_ID (without any splitting)
            case "MEASOBJLDN-VARCHAR2":
                keysValues.put("ENODEBID",
                        CommonLibrary.networkIdGenerator(
                                neRawId,
                                neRawId,
                                "NODEB",
                                vendorId));
                keysValues.put("NETWORK_ID", measObj);
                break;

            case "BOARD-NUMBER":
                keysValues.put("NETWORK_ID",
                        CommonLibrary.networkIdGenerator(
                                neRawId,
                                neRawId,
                                "NODEB",
                                vendorId));
                keysValues.put("BOARD_NAME", measObj.split("\\/")[1].split(":")[1]);
                break;

            // generating CELL ID as NETWORK_ID
            case "CELL-NUMBER":
                keysValues.put("ENODEBID",
                        CommonLibrary.networkIdGenerator(
                                neRawId,
                                neRawId,
                                "NODEB",
                                vendorId));
                keysValues.put("NETWORK_ID",
                        CommonLibrary.networkIdGenerator(
                                neRawId,
                                Integer.parseInt(measObjList.get("local cell id")),
                                "CELL",
                                vendorId));
                ParserEngine_pm_Huawei.cellCounter.put(dataDate + "|" + neName + measObjList.get("local cell id"), true);
                break;

            // generating CELL ID as CELLID and NBR CELL ID as NCELLID
            case "NCELL-NUMBER":
                keysValues.put("ENODEBID",
                        CommonLibrary.networkIdGenerator(
                                neRawId,
                                neRawId,
                                "NODEB",
                                vendorId));
                keysValues.put("NETWORK_ID",
                        CommonLibrary.networkIdGenerator(
                                neRawId,
                                Integer.parseInt(measObjList.get("local cell id")),
                                "CELL",
                                vendorId));

                String targetCI;
                switch (measObjList.get("app.measObjLdn.Type")) {
                    case "NCell":
                        targetCI = measObjList.get("cell id");
                        keysValues.put("TARGET_ENODEBID", measObjList.get("enodeb id"));
                        break;
                    case "ECELL_GCELL":
                        targetCI = measObjList.get("geran cell id");
                        keysValues.put("TARGET_LAC", measObjList.get("location area code"));
                        break;
                    case "ECELL_WCELL":
                        targetCI = measObjList.get("rnc cell id");
                        keysValues.put("TARGET_RNCID", measObjList.get("rnc id"));
                        break;
                    default:
                        targetCI = measObj.toLowerCase().split("cell id=")[1].split("\\,")[0];
                }

                keysValues.put("TARGET_CI", targetCI);
                keysValues.put("TARGET_MCC", measObjList.get("mobile country code"));
                keysValues.put("TARGET_MNC", measObjList.get("mobile network code"));
                break;

            // nodeb localcell tablolari icin
            case "NODEB-NUMBER":
                nodebName = measObjList.get("nodeb function name").split("\\/")[0];
                rncNeId = neNameAndIdList3G.get(nodebName);
                if (rncNeId != null) {
                    keysValues.put("RNCID", rncNeId.split("\\|")[2]);
                    keysValues.put("NETWORK_ID", rncNeId.split("\\|")[0]);
                } else {
                    isMissing = true;
                }
                break;

            case "LOCALCELL-NUMBER":
                nodebName = measObjList.get("nodeb function name");
                rncNeId = neNameAndIdList3G.get(nodebName);
                if (rncNeId != null) {
                    keysValues.put("RNCID", rncNeId.split("\\|")[2]);
                    keysValues.put("NETWORK_ID", rncNeId.split("\\|")[0]);
                    keysValues.put("LOCAL_CELL_ID",
                            CommonLibrary.networkIdGenerator(
                                    Integer.parseInt(
                                            rncNeId.split("\\|")[2]
                                                    .split("000")[rncNeId.split("\\|")[2].split("000").length - 1]),
                                    Integer.parseInt(measObjList.get("local cell id")),
                                    "CELL",
                                    4));
                } else {
                    isMissing = true;
                }
                break;

            case "NRCELL-NUMBER":
                keysValues.put("GNODEBID",
                        CommonLibrary.networkIdGenerator(
                                neRawId,
                                neRawId,
                                "NODEB",
                                7));
                keysValues.put("NETWORK_ID",
                        CommonLibrary.networkIdGenerator(
                                neRawId,
                                Integer.parseInt(measObjList.get("nr cell id")),
                                "CELL",
                                7));
                ParserEngine_pm_Huawei.cellCounter.put(dataDate + "|" + neName + measObjList.get("nr cell id"), true);
                break;

            case "NRDUCELL-NUMBER":
                keysValues.put("GNODEBID",
                        CommonLibrary.networkIdGenerator(
                                neRawId,
                                neRawId,
                                "NODEB",
                                7));
                keysValues.put("NETWORK_ID",
                        CommonLibrary.networkIdGenerator(
                                neRawId,
                                Integer.parseInt(measObjList.get("nr du cell id")),
                                "CELL",
                                7));
                ParserEngine_pm_Huawei.cellCounter.put(dataDate + "|" + neName + measObjList.get("nr du cell id"), true);
                break;
        }

        if (isMissing) {
            keysValues.clear();
            //System.out.println("missing nodeB: " + nodebName + " at " + measInfo + " of " + dataType + " value: " + measObj);
        }

        return keysValues;
    }

}
