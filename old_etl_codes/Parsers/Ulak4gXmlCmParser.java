/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_cm_ULAK4G.incrementCounter;
import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_cm_ULAK4G.neNameAndIdList;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import static com.ttgint.parserEngine.common.AbsParserEngine.vendorID;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ParserIOException;
import com.ttgint.parserEngine.parserHandler.SaxParserHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import com.ttgint.parserEngine.systemProperties.RanElementsInfo;
import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author ibrahimegerci
 */
public class Ulak4gXmlCmParser extends SaxParserHandler {

    private String tag;
    private String dataDate;
    private HashMap<String, String> eNodeB;
    private HashMap<String, String> sector;
    private HashMap<String, String> cell;
    private boolean eNodeBFlag = false;
    private boolean sectorFlag = false;
    private boolean cellFlag = false;
    private static HashSet<String> newNELs = new HashSet<>();
    private static String fileName;

    public Ulak4gXmlCmParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void onStartParseOperation() {
        dataDate = currentFileProgress.getName().split("\\_")[1].replace(".xml", "").replace("-", "");
        this.fileName = currentFileProgress.getName();
    }

    @Override
    public void onstopParseOperation() {

        String ls = "";
        for (String neName : newNELs) {
            if (neName != null && !neName.isEmpty() && !neName.equals("null")) {
                ls += neName + "\n";
            }
        }
        if (!ls.isEmpty()) {
            if (ls.length() > 3000) {
                ls = ls.substring(0, 2998);
            }
            dbHelper.insertParserException(ls);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tag = "";
        switch (qName) {
            case "eNBConfigurationTable":
                eNodeBFlag = true;
                sectorFlag = false;
                cellFlag = false;
                eNodeB = new HashMap<>();
                eNodeB.put("DATA_DATE", dataDate);
                eNodeB.put("eNBConfigurationTableInst", attributes.getValue("inst"));
                break;
            case "SectorInfoTable":
                eNodeBFlag = false;
                sectorFlag = true;
                cellFlag = false;
                sector = new HashMap<>();
                sector.put("SectorInfoTableInst", attributes.getValue("inst"));
                break;
            case "CellInfoTable":
                eNodeBFlag = false;
                sectorFlag = false;
                cellFlag = true;
                cell = new HashMap<>();
                cell.put("CellInfoTableInst", attributes.getValue("inst"));
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tag += new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        switch (qName) {
            case "CellInfoTable":
                cell.putAll(eNodeB);
                cell.putAll(sector);
                writeDataIntoFile(cell, "CELL");
                cell.clear();
                cellFlag = false;
                break;
            case "SectorInfoTable":
                sector.clear();
                sectorFlag = false;
                break;
            case "eNBConfigurationTable":
                writeDataIntoFile(eNodeB, "ENODEB");
                eNodeB.clear();
                eNodeBFlag = false;
                break;
            default:
                if (cellFlag) {
                    cell.put(qName, tag);
                } else if (sectorFlag) {
                    sector.put(qName, tag);
                } else if (eNodeBFlag) {
                    eNodeB.put(qName, tag);
                }
        }
    }

    private void writeDataIntoFile(HashMap<String, String> list, String neType) {

        if (neType.equals("CELL")) {
            if (!list.get("Cell_Status").equals("ACTIVE")) {
                return;
            }
        }

        String neName = list.get("eNB_Name");
        String neNameCode = "";
        try {
            neNameCode = neName.split("\\_")[0] + "_" + neName.split("\\_")[1];
        } catch (Exception e) {
            System.out.println(fileName + ":Wrong NE Name Format! :" + neName);
            newNELs.add(fileName + ":Wrong NE Name Format! :" + neName);
            return;
        }

        int rawNeId = checkNeName(neName, neNameCode);
        if (rawNeId == 0) {
            return;
        }

        String eNodebId = networkIdGenerator(rawNeId, rawNeId, "ENODEB");
        String networkId = eNodebId;
        String neTypeId = Integer.toString(RanElementsInfo.BTSorNB.getNeTypeId());

        if (neType.equals("CELL")) {
            networkId = networkIdGenerator(rawNeId, Integer.parseInt(list.get("Cell_Id")), "CELL");
            neName = list.get("Cell_Name");
            neTypeId = Integer.toString(RanElementsInfo.CELL.getNeTypeId());
        }

        list.put("NE_ID", networkId);
        list.put("PARENT_ID", eNodebId);
        list.put("TOP_PARENT_ID", eNodebId);
        list.put("NE_NAME", neName);
        list.put("NE_TYPE", neTypeId);

        String fullHeader = "";
        String fullValues = "";
        for (String key : list.keySet()) {
            fullHeader += key + AbsParserEngine.resultParameter;
            fullValues += list.get(key) + AbsParserEngine.resultParameter;
        }
        fullHeader = fullHeader.substring(0, fullHeader.length() - 1);
        fullValues = fullValues.substring(0, fullValues.length() - 1);

        RawTableObject tableObject = TableWatcher.getInstance().getTableObjectFromTableName("OBJECTS_ULAK4G_DAILY");
        String dbColumns = tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter);

        String fileOutputName = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;
        String record = CommonLibrary.get_RecordValue(fullHeader, fullValues, dbColumns, "", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter);

        try {
            writeIntoFilesWithController(fileOutputName, record + "\n");
        } catch (ParserIOException ex) {

        }

    }

    private String networkIdGenerator(int parentID, int childID, String neType) {

        int neClassTypeID = 0;
        switch (neType) {
            case "ENODEB":
                neClassTypeID = RanElementsInfo.BTSorNB.getNeTypeId();
                break;
            case "CELL":
                neClassTypeID = RanElementsInfo.CELL.getNeTypeId();
                break;
        }

        BigDecimal bd = new BigDecimal("10");
        BigDecimal resultBigDecimal;

        resultBigDecimal = bd.pow(21).multiply(BigDecimal.valueOf(vendorID)).
                add(bd.pow(16).multiply(BigDecimal.valueOf(neClassTypeID))).
                add(bd.pow(8).multiply(BigDecimal.valueOf(parentID))).
                add(BigDecimal.valueOf(childID));

        return resultBigDecimal.toString();
    }

    private static synchronized int checkNeName(String neName, String neNameCode) {
        int rawNeId;
        if (neNameAndIdList.containsKey(neNameCode)) {
            rawNeId = Integer.parseInt(neNameAndIdList.get(neNameCode).get(0));
        } else { //new NE
            rawNeId = incrementCounter();
            boolean flag = AbsParserEngine.dbHelper.setNewRawNe(AbsParserEngine.systemType, AbsParserEngine.operatorName, neName, rawNeId);
            if (flag) {
                ArrayList<String> row = new ArrayList<>();
                row.add(Integer.toString(rawNeId));
                row.add(neName);
                row.add("1");
                neNameAndIdList.put(neNameCode, row);
                System.out.println("*New NE Created! :" + neName);
            } else {
                System.out.println(fileName + " : Create New NE Failed! :" + neName);
                newNELs.add(fileName + " : Create New NE Failed! :" + neName);
                return 0;
            }
        }
        return rawNeId;
    }
}
