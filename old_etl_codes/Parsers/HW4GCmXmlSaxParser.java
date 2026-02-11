/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_cm_Huawei;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.parserHandler.SaxParserHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author ibrahimegerci
 */
public class HW4GCmXmlSaxParser extends SaxParserHandler {

    private String tagValue;
    private String tagKey;

    private final String neName;
    private final int neRawID;
    private final String dataDate;
    private final int neVendorId;

    private int nodeFlag = 0;
    private String elementType;
    private HashMap<String, String> nodeMap = new HashMap<>();
    private HashMap<String, String> subNodeMap = new HashMap<>();

    public HW4GCmXmlSaxParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType,
            String neName, int neRawID, String dataDate, int vendorId) {
        super(currentFileProgress, operationSystem, progType);
        this.neName = neName;
        this.neRawID = neRawID;
        this.dataDate = dataDate;
        this.neVendorId = vendorId;
    }

    @Override
    public void onStartParseOperation() {
    }

    @Override
    public void onstopParseOperation() {

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tagValue = "";
        switch (qName) {
            case "MO":
                if (nodeFlag == 0) {// parse for BSC,RNC,eNodeB,gNodeB
                    elementType = "node";
                    nodeMap.put(elementType + ".key.className", attributes.getValue("className"));
                    nodeMap.put(elementType + ".key.neName", neName);
                    nodeMap.put(elementType + ".key.neRawID", Integer.toString(neRawID));
                    nodeMap.put(elementType + ".key.dataDate", dataDate);
                } else if (nodeFlag > 0) {// parse for TRX,Cell,BTS,NodeB
                    elementType = "subNode";
                    subNodeMap.put(elementType + ".key.className", attributes.getValue("className"));
                }

                String key = "";
                String value = "";
                String splitter = ",";
                if (tagValue.contains(", ")) {
                    splitter = ", ";
                }
                for (String each : attributes.getValue("fdn").split("\\" + splitter)) {
                    if (each.contains("=")) {
                        key = elementType + ".key.fdn." + each.split("\\=")[0];
                        value = each.split("\\=")[1];
                    } else {
                        key = elementType + ".key.fdn";
                        value = each;
                    }

                    if (key.startsWith("node")) {
                        if (nodeMap.containsKey(key)) {
                            key = key + "_" + value;
                        }
                        nodeMap.put(key.replace("\t", " ").replace("\n", " ").trim(),
                                value.replace("\t", " ").replace("\n", " ").trim());
                    } else {
                        if (subNodeMap.containsKey(key)) {
                            key = key + "_" + value;
                        }
                        subNodeMap.put(key.replace("\t", " ").replace("\n", " ").trim(),
                                value.replace("\t", " ").replace("\n", " ").trim());
                    }
                }

//                for (String each : attributes.getValue("fdn").split("\\,")) {
//                    nodeMap.put(elementType + ".key.fdn." + each.split("\\=")[0].trim(), each.split("\\=")[1].trim());
//                }
                nodeFlag++;
                break;
            case "attr":
                tagKey = elementType + ".value." + attributes.getValue("name");
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
            case "MO":
                if (subNodeMap.size() > 0) {
                    subNodeMap.putAll(nodeMap);
                    writeDataIntoFile(subNodeMap, "subNode");
                    subNodeMap.clear();
                }
                break;
            case "MOTree":
                writeDataIntoFile(nodeMap, "node");
                try {
                    //Update ranversion
                    String neVersion = nodeMap.get("node.value.neVersion");
                    neVersion = neVersion.substring(neVersion.indexOf("R"), neVersion.indexOf("C"));
                    dbHelper.updateRanVersionHw(AbsParserEngine.operatorName, AbsParserEngine.systemType, neName, neVersion);
                } catch (Exception ex) {
                }
                break;
            case "attr":
                String key = "";
                String value = "";
                String splitter = ",";
                if (tagValue.contains(", ")) {
                    splitter = ", ";
                }
                for (String each : tagValue.split("\\" + splitter)) {
                    if (each.contains("=")) {
                        key = tagKey + "." + each.split("\\=")[0];
                        value = each.split("\\=")[1];
                    } else {
                        key = tagKey;
                        value = tagValue;
                    }

                    if (key.startsWith("node")) {
                        if (nodeMap.containsKey(key)) {
                            key = key + "_" + value;
                        }
                        nodeMap.put(key.replace("\t", " ").replace("\n", " ").trim(),
                                value.replace("\t", " ").replace("\n", " ").trim());
                    } else {
                        if (subNodeMap.containsKey(key)) {
                            key = key + "_" + value;
                        }
                        subNodeMap.put(key.replace("\t", " ").replace("\n", " ").trim(),
                                value.replace("\t", " ").replace("\n", " ").trim());
                    }
                }
                break;
        }
    }

    private void writeDataIntoFile(HashMap<String, String> list, String elementType) {
        try {
            RawTableObject tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(list.get(elementType + ".key.className"));
            if (tableObject != null) {
                String neId = "";
                String parentId = "";
                //String topParentId = "";
                switch (tableObject.getTableType()) {
                    case "ENODEB-CELL":
                    case "GNODEB-CELL":
                        neId = networkIdGenerator(Integer.valueOf(list.get("node.key.neRawID")),
                                Integer.valueOf(list.get(tableObject.getNetworkIdType())),
                                getClassTypeId(tableObject.getTableType()), getVendorId(tableObject.getNeType()));

                        parentId = networkIdGenerator(Integer.valueOf(list.get("node.key.neRawID")),
                                Integer.valueOf(list.get("node.key.neRawID")),
                                getClassTypeId(tableObject.getTableType().split("\\-")[0]), getVendorId(tableObject.getSystemType()));

                        list.put("object.key.neId", neId);
                        list.put("object.key.parentId", parentId);
                        list.put("object.key.topparentId", parentId);
                        list.put("object.key.type", Integer.toString(ParserEngine_cm_Huawei.cellClassTypeId));
                        list.put("object.key.neName", list.get(tableObject.getFunctionSubsetId()));
                        break;
                    case "ENODEB":
                        neId = networkIdGenerator(Integer.valueOf(list.get("node.key.neRawID")),
                                Integer.valueOf(list.get(tableObject.getNetworkIdType())),
                                getClassTypeId(tableObject.getTableType()), getVendorId(tableObject.getNeType()));
                        list.put("object.key.neId", neId);
                        list.put("object.key.parentId", neId);
                        list.put("object.key.topparentId", neId);
                        list.put("object.key.type", Integer.toString(ParserEngine_cm_Huawei.eNodeBClassTypeId));
                        list.put("object.key.neName", list.get(tableObject.getFunctionSubsetId()));
                        break;
                }

                writeIntoFilesWithController(AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension, //Path
                        CommonLibrary.get_RecordValue(
                                String.join(AbsParserEngine.resultParameter, list.keySet()), //counter
                                String.join(AbsParserEngine.resultParameter, list.values()), //value
                                tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter), //counterDB
                                "", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter) + "\n");

                //Added for OBJECTS Table
                RawTableObject tableObjectAll = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName("ALL");
                if (tableObject != null
                        && (!tableObject.getTableType().contains("CELL")
                        || (tableObject.getTableType().contains("CELL") && list.get("subNode.value.CELLACTIVESTATE").equals("CELL_ACTIVE")))) {
                    writeIntoFilesWithController(AbsParserEngine.LOCALFILEPATH + tableObjectAll.getTableName() + AbsParserEngine.integratedFileExtension, //Path
                            CommonLibrary.get_RecordValue(
                                    String.join(AbsParserEngine.resultParameter, list.keySet()), //header
                                    String.join(AbsParserEngine.resultParameter, list.values()), // record
                                    tableObjectAll.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter), // headerDB
                                    "", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter) + "\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String networkIdGenerator(int parentId, int childId, int neTypeId, int vendorId) {
        BigDecimal bd = new BigDecimal("10");
        BigDecimal resultBigDecimal = bd.pow(21).multiply(BigDecimal.valueOf(vendorId)).
                add(bd.pow(16).multiply(BigDecimal.valueOf(neTypeId))).
                add(bd.pow(8).multiply(BigDecimal.valueOf(parentId))).
                add(BigDecimal.valueOf(childId));

        return resultBigDecimal.toString();
    }

    private int getClassTypeId(String neType) {
        int classTypeId = 1;
        switch (neType) {
            case "TRX":
                classTypeId = ParserEngine_cm_Huawei.trxClassTypeId;
                break;
            case "CELL":
            case "ENODEB-CELL":
            case "GNODEB-CELL":
                classTypeId = ParserEngine_cm_Huawei.cellClassTypeId;
                break;
            case "BTS":
                classTypeId = ParserEngine_cm_Huawei.btsClassTypeId;
                break;
            case "NODEB":
                classTypeId = ParserEngine_cm_Huawei.nodeBClassTypeId;
                break;
            case "BSC":
                classTypeId = ParserEngine_cm_Huawei.bscClassTypeId;
                break;
            case "RNC":
                classTypeId = ParserEngine_cm_Huawei.rncClassTypeId;
                break;
            case "ENODEB":
                classTypeId = ParserEngine_cm_Huawei.eNodeBClassTypeId;
                break;
            case "GNODEB":
                classTypeId = ParserEngine_cm_Huawei.gNodeBClassTypeId;
                break;
        }
        return classTypeId;
    }

    private int getVendorId(String neType) {
        int vendorId = 1;
        switch (neType) {
            case "BTS":
            case "BSC":
            case "HW2G":
                vendorId = ParserEngine_cm_Huawei.vendorId2G;
                break;
            case "NODEB":
            case "RNC":
            case "HW3G":
                vendorId = ParserEngine_cm_Huawei.vendorId3G;
                break;
            case "ENODEB":
            case "HW4G":
                vendorId = ParserEngine_cm_Huawei.vendorId4G;
                break;
            case "GNODEB":
            case "HW5G":
                vendorId = ParserEngine_cm_Huawei.vendorId5G;
                break;
        }
        return vendorId;
    }
}
