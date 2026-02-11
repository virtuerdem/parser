/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_NecSpiderNet;
import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_NecSpiderNet.objects;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.LOCALFILEPATH;
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
import java.util.HashMap;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author turgut.simsek
 */
public class NecSpiderNetXmlParser extends SaxParserHandler {

    private NecSpiderNetSubset subset;
    private String tag;
    private int counterNameIndex;
    private int counterValueIndex;
    private String date;

    public NecSpiderNetXmlParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void onStartParseOperation() {
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tag = "";
        
        switch (qName) {
            case "measInfo":
                subset = new NecSpiderNetSubset();
                subset.setDataDate(date);
                break;
            case "measCollec":

                if (attributes.getValue("beginTime") != null) {
                    date = attributes.getValue("beginTime");
                    date = date.replace("-", "").replace(":", "").replace("T", "").substring(0, 12); // endTime="2017-08-08T14:55:00+03:00"
                }

                break;
            case "measType":
                counterNameIndex = Integer.parseInt(attributes.getValue("p"));
                break;

            case "r":
                counterValueIndex = Integer.parseInt(attributes.getValue("p"));
                break;

            case "measValue":
                String measObject = attributes.getValue("measObjLdn");
                subset.setMeasObject(measObject);

                String[] measArray = measObject.split("\\,");
                String funcName = measArray[measArray.length - 1].split("\\=")[0];
                RawTableObject tableObject = tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(funcName);
                subset.setTableObject(tableObject);
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

            case "measType":
                subset.addCounterIndexNameMap(counterNameIndex, tag);
                break;
            case "r":
                subset.addCounterIndexValueMap(counterValueIndex, tag);
                break;

            case "measValue":
                if (subset.getTableObject() != null) {
                    generateConstantValue(subset.getTableObject());
                    writeDataIntoFile();
                }
                subset.resetCounterValues();
                break;
        }

    }

    private void generateConstantValue(RawTableObject tableObject) {

        int rawNeId;
        String neName = "";
        String enodebId = "";
        String networkID = "";
        int neClassTypeID = 0;

        HashMap<String, String> idValueMap = new HashMap<>();

        switch (tableObject.getTableType()) {

            case "CELL": // EUtranFunction=VFSN01,EUtranCellFDD=Cell-2"
                neName = subset.getMeasObject().split("\\,")[0].split("\\=")[1];
                rawNeId = Integer.parseInt(ParserEngine_pm_NecSpiderNet.neNameAndIdList.get(neName));

                String cellName = subset.getMeasObject().split("\\,")[1].split("\\=")[1];
                int cellId = Integer.parseInt(subset.getMeasObject().split("\\-")[1]);

                enodebId = networkIdGenerator(rawNeId, rawNeId, "NODEB");
                networkID = networkIdGenerator(rawNeId, cellId, "CELL");
                neClassTypeID = RanElementsInfo.CELL.getNeTypeId();

                subset.getFileHeaderValueMap().put("DATA_DATE", subset.getDataDate());
                subset.getFileHeaderValueMap().put("NETWORK_ID", networkID);
                subset.getFileHeaderValueMap().put("ENODEBID", enodebId);

                idValueMap.put(networkID, new ObjectsRaw(subset.getDataDate(), networkID, enodebId, enodebId, cellName, neClassTypeID).getRaw());
                idValueMap.put(enodebId, new ObjectsRaw(subset.getDataDate(), enodebId, enodebId, enodebId, neName, RanElementsInfo.BTSorNB.getNeTypeId()).getRaw());

                addObjectMap(date, idValueMap);

                break;
            case "ENODEB": // "EUtranFunction=VFSN01"

                neName = subset.getMeasObject().split("\\=")[1];
                rawNeId = Integer.parseInt(ParserEngine_pm_NecSpiderNet.neNameAndIdList.get(neName));
                enodebId = networkIdGenerator(rawNeId, rawNeId, "NODEB");

                subset.getFileHeaderValueMap().put("DATA_DATE", subset.getDataDate());
                subset.getFileHeaderValueMap().put("NETWORK_ID", enodebId);

                idValueMap.put(enodebId, new ObjectsRaw(subset.getDataDate(), enodebId, enodebId, enodebId, neName, RanElementsInfo.BTSorNB.getNeTypeId()).getRaw());

                addObjectMap(date, idValueMap);
                break;

        }

    }

    private void addObjectMap(String date, HashMap<String, String> idValeuMap) {

        if (objects.containsKey(date)) {
            for (String key : idValeuMap.keySet()) {
                objects.get(date).put(key, idValeuMap.get(key));
            }
        } else {
            objects.put(date, idValeuMap);
        }
    }

    private String networkIdGenerator(int parentID, int childID, String neType) {

        int neClassTypeID = 0;
        switch (neType) {
            case "NODEB":
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

    private void writeDataIntoFile() {

        RawTableObject tableObject = subset.getTableObject();
        if (tableObject != null) {

            String tableColumnNames = tableObject.getFullColumnOrderUsingCounterNameLookup(AbsParserEngine.resultParameter);

            String fileHeaderNames = subset.getFileHeaderNames(AbsParserEngine.resultParameter);
            String record = subset.getCounterValues(AbsParserEngine.resultParameter);

            String fullPath = LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;

            try {
                writeIntoFilesWithController(fullPath, CommonLibrary.get_RecordValue(fileHeaderNames, record, tableColumnNames, "0", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter) + "\n");
            } catch (ParserIOException ex) {
                dbHelper.insertParserException(ex.getMessage());
            }
        }
    }

    @Override
    public void onstopParseOperation() {
    }

    private class ObjectsRaw {

        private final String DATA_DATE;
        private final String NE_ID;
        private final String PARENT_ID;
        private final String TOP_PARENT_ID;
        private final String NE_NAME;
        private final int NE_TYPE;

        public ObjectsRaw(String DATA_DATE, String NE_ID, String PARENT_ID, String TOP_PARENT_ID, String NE_NAME, int NE_TYPE) {
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

}
