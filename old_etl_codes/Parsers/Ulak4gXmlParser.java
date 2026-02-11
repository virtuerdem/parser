/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_ULAK4G.incrementCounter;
import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_ULAK4G.neNameAndIdList;
import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_ULAK4G.objects;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.LOCALFILEPATH;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import static com.ttgint.parserEngine.common.AbsParserEngine.vendorID;
import com.ttgint.parserEngine.common.RawCounterObject;
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author turgut.simsek
 */
public class Ulak4gXmlParser extends SaxParserHandler {

    private RawTableObject tableObject;
    private Ulak4gSubset subset;
    private String tag;
    private static HashSet<String> newNELs = new HashSet<>();
    private static String fileName;

    public Ulak4gXmlParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void onStartParseOperation() {
        this.fileName = currentFileProgress.getName();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tag = "";

        switch (qName) {
            case "measInfo":
                subset = new Ulak4gSubset();
                break;
            case "granPeriod":
                String date = attributes.getValue("endTime");
                date = date.replace("-", "").replace(":", "").replace("T", "").substring(0, 12); // endTime="2017-08-08T14:55:00+03:00"
                Calendar cal = Calendar.getInstance();
                try {
                    cal.setTime(new SimpleDateFormat("yyyyMMddHHmm").parse(date));
                } catch (ParseException ex) {
                    ex.printStackTrace();
                }
                cal.add(Calendar.HOUR, -1);
                subset.setDataDate(new SimpleDateFormat("yyyyMMddHHmm").format(cal.getTime()));
                break;
            case "measValue":
                subset.setMeasObject(attributes.getValue("measObjLdn"));
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
            case "measValue":
                writeDataIntoFile();
                break;
            case "measType":
                subset.addCounterNameList(tag);
                break;
            case "r":
                subset.addCounterValueList(tag);
                break;
            case "measInfo":
                // if (subset.getTableObject() != null) {
                //     AutoCounterDefine.getInstance().setCounters(subset.getTableObject().getFunctionSubsetName(), subset.getCounterNames(), "|");
                // }
                break;
        }
    }

    private void writeDataIntoFile() {

        tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(subset.getFunctionSubsetName());
        if (tableObject != null) {
            if (generateConstantValues(tableObject)) {

                String fileHeaderNames = subset.getFileHeaderNames();
                for (RawCounterObject rawCounterObject : tableObject.getConstantObjectList()) { // add constant 0|0|network|enod|date
                    fileHeaderNames += rawCounterObject.getCounterNameDb() + AbsParserEngine.resultParameter;
                }
                fileHeaderNames = fileHeaderNames.substring(0, fileHeaderNames.length() - 1);
                String record = subset.getCounterValues();

                //column sayisi cok fazla olan tablolar 500erlik sekilde bolunmelidir
                for (int i = 1; i <= (subset.getCounterValueList().size() / 500) + 1; i++) {
                    if (i > 1) {
                        tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(subset.getFunctionSubsetName() + "_" + i);
                    }
                    if (tableObject == null) {
                        break;
                    }

                    String tableColumnNames = tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter);
                    String fullPath = LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;

                    try {
                        writeIntoFilesWithController(fullPath, CommonLibrary.get_RecordValue(fileHeaderNames, record, tableColumnNames, "0", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter) + "\n");
                    } catch (ParserIOException ex) {
                        dbHelper.insertParserException(ex.getMessage());
                    }
                }

                subset.setCounterValueList(new ArrayList<String>());
                subset.setCounterValues("");
            }
        }
    }

    private boolean generateConstantValues(RawTableObject tableObject) {

        HashMap<String, String> idValueMap = new HashMap<>();

        try {
            String neName = subset.getNeName();
            String neNameCode = "";
            try {
                neNameCode = neName.split("\\_")[0] + "_" + neName.split("\\_")[1];
            } catch (Exception e) {
                System.out.println(fileName + " : Wrong NE Name Format! :" + neName);
                newNELs.add(fileName + " : Wrong NE Name Format! :" + neName);
                return false;
            }

            int rawNeId = checkNeName(neName, neNameCode);
            if (rawNeId == 0) {
                return false;
            }

            String enodebId = "";
            String networkID = "";
            int neClassTypeID = 0;
            String[] param = subset.getMeasObject().split("\\,");
            switch (tableObject.getTableType()) {

                // generating CELL ID as NETWORK_ID
                case "CELL":
                    if (param.length == 3) {
                        String cellId = param[2].split("\\=")[1].substring(param[2].split("\\=")[1].length() - 2, param[2].split("\\=")[1].length());
                        String cellName = param[2].split("\\=")[1];

                        enodebId = networkIdGenerator(rawNeId, rawNeId, "ENODEB");
                        networkID = networkIdGenerator(rawNeId, Integer.parseInt(cellId), "CELL");
                        neClassTypeID = RanElementsInfo.CELL.getNeTypeId();

                        subset.addCounterValueList(networkID);
                        subset.addCounterValueList(enodebId);
                        subset.addCounterValueList(subset.getDataDate());

                        idValueMap.put(networkID, new objectsRaw(subset.getDataDate(), networkID, enodebId, enodebId, cellName, neClassTypeID).getRaw());
                        idValueMap.put(enodebId, new objectsRaw(subset.getDataDate(), enodebId, enodebId, enodebId, neName, RanElementsInfo.BTSorNB.getNeTypeId()).getRaw());
                        addObjectMap(subset.getDataDate(), idValueMap);
                    }

                    break;
                case "ENODEB": // <measValue measObjLdn="ManagedElement=ULAK-1,ENodeBFunction=59124">
                    enodebId = networkIdGenerator(rawNeId, rawNeId, "ENODEB");

                    subset.addCounterValueList(enodebId);
                    subset.addCounterValueList(subset.getDataDate());

                    idValueMap.put(enodebId, new objectsRaw(subset.getDataDate(), enodebId, enodebId, enodebId, neName, RanElementsInfo.BTSorNB.getNeTypeId()).getRaw());
                    addObjectMap(subset.getDataDate(), idValueMap);
                    break;
            }
            return true;
        } catch (NumberFormatException e) {
            System.out.println(fileName + " :" + subset.getNeName());
            newNELs.add(fileName + " :" + subset.getNeName());
            return false;
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

    @Override
    public void onstopParseOperation() {
        String ls = "";
        for (String neName : newNELs) {
            if (neName != null && !neName.isEmpty() && !neName.equals("null")) {
                ls += neName + "\n";
            }
        }
        if (!ls.isEmpty()) {
            dbHelper.insertParserException(ls);
        }
    }

    private static synchronized void addObjectMap(String date, HashMap<String, String> idValeuMap) {

        if (objects.containsKey(date)) {
            for (String key : idValeuMap.keySet()) {
                objects.get(date).put(key, idValeuMap.get(key));
            }
        } else {
            objects.put(date, idValeuMap);
        }
    }

    private class objectsRaw {

        private final String DATA_DATE;
        private final String NE_ID;
        private final String PARENT_ID;
        private final String TOP_PARENT_ID;
        private final String NE_NAME;
        private final int NE_TYPE;

        public objectsRaw(String DATA_DATE, String NE_ID, String PARENT_ID, String TOP_PARENT_ID, String NE_NAME, int NE_TYPE) {
            this.DATA_DATE = DATA_DATE;
            this.NE_ID = NE_ID;
            this.PARENT_ID = PARENT_ID;
            this.TOP_PARENT_ID = TOP_PARENT_ID;
            this.NE_NAME = NE_NAME;
            this.NE_TYPE = NE_TYPE;
        }

        public String getRaw() {
            return DATA_DATE + "|" + NE_ID + "|" + PARENT_ID + "|" + TOP_PARENT_ID + "|" + NE_NAME + "|" + NE_TYPE + "\n";
        }

    }

    private static synchronized int checkNeName(String neName, String neNameCode) {
        int rawNeId;
        if (neNameAndIdList.containsKey(neNameCode)) {
            rawNeId = Integer.parseInt(neNameAndIdList.get(neNameCode).get(0));
            if (!neNameAndIdList.get(neNameCode).get(1).equals(neName) || !neNameAndIdList.get(neNameCode).get(2).equals("1")) {//passive or changed name
                AbsParserEngine.dbHelper.updateRawNeNameActive(AbsParserEngine.systemType, AbsParserEngine.operatorName, neName, rawNeId);

                System.out.println("*NE Updated! :" + neNameAndIdList.get(neNameCode).get(1) + " -> " + neName);
                neNameAndIdList.get(neNameCode).set(1, neName);
                neNameAndIdList.get(neNameCode).set(2, "1");
            }
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
