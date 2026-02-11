/*
 * This script parses xml file whose functionsubsetIDs already defined as FunctionSubSet Class.
 * SAX Parsing method is used below. This code is written to high parsing performance.
 * Below code can be used HW RAN 2G and 3G and CS Performance files.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_HW_2g3gcs;
import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_HW_2g3gcs.BSCCLASSTYPEID;
import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_HW_2g3gcs.BTSCLASSTYPEID;
import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_HW_2g3gcs.CELLCLASSTYPEID;
import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_HW_2g3gcs.TRXCLASSTYPEID;
import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_HW_2g3gcs.btsNameToBtsId;
import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_HW_2g3gcs.enodebNameToENodeBId;
import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_HW_2g3gcs.neNameAndIdList;
import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_HW_2g3gcs.nodebNameRncId;
import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_HW_2g3gcs.nodebNameToNodeBId;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.LOCALFILEPATH;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import static com.ttgint.parserEngine.common.AbsParserEngine.systemType;
import static com.ttgint.parserEngine.common.AbsParserEngine.vendorID;
import com.ttgint.parserEngine.common.AutoCounterDefine;
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
import java.util.ArrayList;
import java.util.List;
import org.xml.sax.Attributes;

public class HW2G3GCSPmSaxXmlParser extends SaxParserHandler {

    public final List<String> uniqePartList = new ArrayList<>();
    public ArrayList<HW2G3GCSFunctionSubSet> fssList;
    public HW2G3GCSFunctionSubSet fssTmp = null;
    private String bscType;
    private String value;
    private String dataDate;
    private String neName;
    private Boolean lteNodebFlag = false;
    private Boolean gNodebFlag = false;
    private String tempMeasResult;
    private String tempMeasObjLdn;
    private boolean isSuspect = false;

    public HW2G3GCSPmSaxXmlParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void onStartParseOperation() {
        fssList = new ArrayList<>();

        String splitted[] = currentFileProgress.getName().split("\\_");
        neName = currentFileProgress.getName().replace(splitted[0] + "_", "");
        neName = neName.replace(".xml", "");
        dataDate = currentFileProgress.getName().substring(1, 14).replace(".", "");

        //Active sharing sahalar vfd -> digerOperatorler
        if (currentFileProgress.getName().charAt(1) == '-') {
            dataDate = currentFileProgress.getName().substring(3, 16).replace(".", "");
        }

        if (AbsParserEngine.operatorName.equals("KKTC-TELSIM") && systemType.equals("HW3G")) {
            neName = neName.split("\\_")[0];
        }
    }

    @Override
    public void onstopParseOperation() {
    }

    @Override
    public void startElement(String s, String s1, String elementName, Attributes attributes) {
        value = "";

        if (elementName.equalsIgnoreCase("measInfo")) {
            String functionSubsetId = attributes.getValue("measInfoId");
            RawTableObject tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetId(functionSubsetId);
            if (tableObject instanceof RawTableObject) {
                fssTmp = new HW2G3GCSFunctionSubSet();
                fssTmp.setFunctionSubSetId(attributes.getValue("measInfoId"));
                fssTmp.setDataDate(dataDate);
                fssTmp.setNeName(neName);
                fssTmp.setFunctionSubSetName(tableObject.getTableName());
                fssTmp.setTableObject(tableObject);
            } else {
                fssTmp = new HW2G3GCSFunctionSubSet();
                fssTmp.setFunctionSubSetId(attributes.getValue("measInfoId"));
            }
        }

        if (elementName.equalsIgnoreCase("measValue") && fssTmp != null) {
            tempMeasObjLdn = attributes.getValue("measObjLdn");
        }

        if (elementName.equals("suspect") && fssTmp != null) {
            isSuspect = false;
        }

    }

    @Override
    public void characters(char[] ac, int i, int j) {
        value += new String(ac, i, j);
    }

    @Override
    public void endElement(String s, String s1, String element) {

        if (element.equals("measInfo") && fssTmp != null) {
            try {
                if (fssTmp.getFunctionSubSetName() != null) {
                    writeDataIntoFile(fssTmp);
                }
            } catch (IOException | ParserIOException ex) {
                dbHelper.insertParserException(ex.getMessage());

            }
            fssTmp = null;
        }

        if (element.equals("measTypes") && fssTmp != null) {
            fssTmp.setCounterNames(value);
            AutoCounterDefine.getInstance().setCounters(fssTmp.getFunctionSubSetId(), value.trim(), " ");
        }

        if (element.equals("measResults") && fssTmp != null) {
            if (AbsParserEngine.systemType.equals("HWCS")) {
                StringBuilder sbuilder = new StringBuilder();
                String[] splitterString = value.split("\"");
                for (String str : splitterString) {
                    if (str.contains("-") && str.contains(":")) {
                        sbuilder.append(str.replace(str, "0"));
                    } else {
                        sbuilder.append(str);
                    }
                }
                value = sbuilder.toString();
            }
            tempMeasResult = value;
        }

        if (element.equalsIgnoreCase("measValue") && fssTmp != null) {
            AutoCounterDefine.getInstance().setObjects(fssTmp.getFunctionSubSetId(), tempMeasObjLdn);

            if (isSuspect == false) {
                fssTmp.addCounterValues(tempMeasResult);
                fssTmp.addMeasValue(tempMeasObjLdn);
                tempMeasObjLdn = null;
                tempMeasResult = null;
            }
            isSuspect = false;
            //  System.out.println(attributes.getValue("measObjLdn") + " " + neName + " " );
        }
    }

    private void writeDataIntoFile(HW2G3GCSFunctionSubSet fss1) throws IOException, ParserIOException {

        //System.out.println("Data date  : " + fss1.getDataDate());
        String txtFileName = LOCALFILEPATH + fss1.getFunctionSubSetName() + AbsParserEngine.integratedFileExtension;
        StringBuilder sb = new StringBuilder();

        TableWatcher watcher = TableWatcher.getInstance();
        RawTableObject tableObject = watcher.getTableObjectFromTableName(fss1.getFunctionSubSetName());

        //fss1.setFunctionSubSetNorthiCounters();
        //String tableColumns = fss1.getFunctionSubSetNorthiCounters();
        String variableColumns = null;
        String constantColumns = null;
        try {
            variableColumns = tableObject.getTableVariableColumns("|");
        } catch (Exception e) {
            System.err.println("Error Table : " + tableObject.getTableName());
        }
        try {
            constantColumns = tableObject.getTableConstantColumns("|");
        } catch (Exception e) {
            System.err.println("Error Table : " + tableObject.getTableName());
        }

        String functionSubSetHeader = fss1.getFunctionSubSetColumns() + "C" + fss1.getCounterNames().trim();
        functionSubSetHeader = functionSubSetHeader.replace(" ", "|C");

        for (int i = 0; i < fss1.getCounterValues().size(); i++) {

            String measObjLdn = (String) fss1.getMeasValue().get(i);
            //Yeni gelen rnc tipi icin logicRNCid alinmiyor simdilik
            if (measObjLdn.contains("LogicRNCID")) {
                bscType = "BSC6910";
                measObjLdn = measObjLdn.substring(0, measObjLdn.indexOf(", LogicRNCID")) + measObjLdn.substring(measObjLdn.indexOf(", LogicRNCID") + ", LogicRNCID".length() + 5);
            } else {
                bscType = "BSC6900";
            }
            String networkID = null;
            String networkParameters = null;
            int rncbscID;
            //HWCS icin networkId generate edilmez
            if (AbsParserEngine.systemType.equals("HWCS")) {
                rncbscID = 0;
            } else {
                try {
                    rncbscID = Integer.parseInt(neNameAndIdList.get(fss1.getNeName()));
                } catch (Exception ex) {
                    System.out.println("NE not found in ParserUsedNes table: " + fss1.getNeName());
                    System.err.println("NE not found in ParserUsedNes table: " + fss1.getNeName());
                    return;
                }
            }

            switch (systemType) {
                case "HWCS":
                    switch (fss1.getTableType()) {
                        case "MEASOBJLDN-VARCHAR2":
                            networkID = measObjLdn;
                            networkParameters = networkID;
                            break;
                    }
                    break;
                case "HW5G":
                    String gnodebName = "";
                    gNodebFlag = false;
                    switch (fss1.getTableType()) {
                        // generating measObjLdn string completely as NETWORK_ID (without any splitting)
                        case "MEASOBJLDN-VARCHAR2":
                            networkID = measObjLdn;
                            networkParameters = networkID;
                            break;

                        // generating CELL ID as NETWORK_ID
                        case "CELL-NUMBER":
                            networkID = networkIdGenerator(rncbscID, Integer.parseInt(measObjLdn.split(" Local Cell ID=")[1].split(",")[0].trim()), "CELL", vendorID);
                            networkParameters = networkID;
                            ParserEngine_pm_HW_2g3gcs.cellCounter.put(dataDate + "|" + neName + measObjLdn.split(" Local Cell ID=")[1].split(",")[0].trim(), true);
                            break;

                        // generating CELL ID as CELLID and NBR CELL ID as NCELLID
                        case "NCELL-NUMBER":
                            int cellID = Integer.parseInt(measObjLdn.toLowerCase().split("local cell id=")[1].split("\\,")[0]);
                            String cellIDWithMultiplier = networkIdGenerator(rncbscID, cellID, "CELL", vendorID);

                            int nCellID = Integer.parseInt(measObjLdn.toLowerCase().split("cell id=")[1].split("\\,")[0]);
                            try {
                                //CELL_TO_LTE icin (4->4)
                                int nNeTargetId = Integer.parseInt(measObjLdn.toLowerCase().split("enodeb id=")[1].split("\\,")[0]);
                                networkParameters = String.valueOf(cellIDWithMultiplier) + "|" + String.valueOf(nCellID) + "|" + String.valueOf(nNeTargetId);
                            } catch (Exception ex) {
                                //CELL_TO_WCDMA icin (4->3)
                                if (tableObject.getTableName().contains("WCDMA")) {
                                    int targetRncId = Integer.parseInt(measObjLdn.split("RNC ID=")[1].split("\"")[0]);
                                    networkParameters = String.valueOf(cellIDWithMultiplier) + "|" + String.valueOf(nCellID) + "|" + String.valueOf(targetRncId);
                                    //CELL_TO GERAN icin (4->2)
                                } else {
                                    String targetMCC = measObjLdn.split("Mobile country code=")[1].split("\\,")[0];
                                    String targetMNC = measObjLdn.split("Mobile network code=")[1].split("\\,")[0];
                                    String targetLAC = measObjLdn.split("Location area code=")[1].split("\"")[0];
                                    networkParameters = String.valueOf(cellIDWithMultiplier) + "|" + String.valueOf(nCellID) + "|"
                                            + targetMCC + "|" + targetMNC + "|" + targetLAC;
                                }
                            }
                            break;

                        //nodeb localcell tablolari icin
                        case "NODEB-NUMBER":
                            gNodebFlag = true;
                            gnodebName = measObjLdn.split("\\/")[1].split("\\:")[1].trim();
                            if (gnodebName.contains("=")) {
                                gnodebName = measObjLdn.split("\\/")[1].split("\\:")[1].split("=")[1].trim();
                            }
                            String rncNeId = nodebNameRncId.get(gnodebName);
                            if (rncNeId != null) {
                                String rncNeIdParted = rncNeId.split("000")[rncNeId.split("000").length - 1];
                                rncbscID = Integer.parseInt(rncNeIdParted);
                                networkID = nodebNameToNodeBId.get(gnodebName);
                                networkParameters = networkID + "|" + rncNeId;
                            }
                            break;
                        case "BOARD-NUMBER":
                            gNodebFlag = true;
                            gnodebName = measObjLdn.split("\\/")[0];
                            String boardName = measObjLdn.split("\\/")[1].split(":")[1];
                            networkID = enodebNameToENodeBId.get(gnodebName);
                            if (networkID != null) {
                                if (tableObject.getTableName().startsWith("M2")) {
                                    networkParameters = networkID + "|" + boardName + "|" + "";
                                } else {
                                    networkParameters = networkID + "|" + boardName;
                                }
                            }
                            break;

                        case "LOCALCELL-NUMBER":
                            gNodebFlag = true;
                            gnodebName = measObjLdn.split("NodeB Function Name=")[1].split(",")[0];
                            rncNeId = nodebNameRncId.get(gnodebName);
                            String localCell;
                            if (measObjLdn.contains("Function Name")) {
                                localCell = measObjLdn.split("\\/")[1].split("Local Cell ID=")[1].split("\\,")[0].trim();
                            } else {
                                localCell = measObjLdn.split("\\/")[1].split("\\=")[1].trim();
                            }
                            if (rncNeId != null) {
                                String rncNeIdParted = rncNeId.split("000")[rncNeId.split("000").length - 1];
                                rncbscID = Integer.parseInt(rncNeIdParted);

                                networkID = nodebNameToNodeBId.get(gnodebName);
                                String localCellId = networkIdGenerator(rncbscID, Integer.parseInt(localCell), "CELL", vendorID);
                                networkParameters = networkID + "|" + localCellId + "|" + rncNeId;
                            }
                            break;

                        case "NRCELL-NUMBER":
                            networkID = networkIdGenerator(rncbscID, Integer.parseInt(measObjLdn.split(" NR Cell ID=")[1].split(",")[0].trim()), "CELL", vendorID);
                            networkParameters = networkID;
                            ParserEngine_pm_HW_2g3gcs.cellCounter.put(dataDate + "|" + neName + measObjLdn.split(" NR Cell ID=")[1].split(",")[0].trim(), true);
                            break;

                        case "NRDUCELL-NUMBER":
                            networkID = networkIdGenerator(rncbscID, Integer.parseInt(measObjLdn.split(" NR DU Cell ID=")[1].split(",")[0].trim()), "CELL", vendorID);
                            networkParameters = networkID;
                            ParserEngine_pm_HW_2g3gcs.cellCounter.put(dataDate + "|" + neName + measObjLdn.split(" NR DU Cell ID=")[1].split(",")[0].trim(), true);
                            break;
                    }
                    break;
                case "HW4G":
                    String nodebName = "";
                    lteNodebFlag = false;
                    switch (fss1.getTableType()) {
                        // generating measObjLdn string completely as NETWORK_ID (without any splitting)
                        case "MEASOBJLDN-VARCHAR2":
                            networkID = measObjLdn;
                            networkParameters = networkID;
                            break;

                        // generating CELL ID as NETWORK_ID
                        case "CELL-NUMBER":
                            networkID = networkIdGenerator(rncbscID, Integer.parseInt(measObjLdn.split(" Local Cell ID=")[1].split(",")[0].trim()), "CELL", vendorID);
                            networkParameters = networkID;
                            ParserEngine_pm_HW_2g3gcs.cellCounter.put(dataDate + "|" + neName + measObjLdn.split(" Local Cell ID=")[1].split(",")[0].trim(), true);
                            break;

                        // generating CELL ID as CELLID and NBR CELL ID as NCELLID
                        case "NCELL-NUMBER":
                            int cellID = Integer.parseInt(measObjLdn.toLowerCase().split("local cell id=")[1].split("\\,")[0]);
                            String cellIDWithMultiplier = networkIdGenerator(rncbscID, cellID, "CELL", vendorID);

                            int nCellID = Integer.parseInt(measObjLdn.toLowerCase().split("cell id=")[1].split("\\,")[0]);
                            try {
                                //CELL_TO_LTE icin (4->4)
                                int nNeTargetId = Integer.parseInt(measObjLdn.toLowerCase().split("enodeb id=")[1].split("\\,")[0]);
                                networkParameters = String.valueOf(cellIDWithMultiplier) + "|" + String.valueOf(nCellID) + "|" + String.valueOf(nNeTargetId);
                            } catch (Exception ex) {
                                //CELL_TO_WCDMA icin (4->3)
                                if (tableObject.getTableName().contains("WCDMA")) {
                                    int targetRncId = Integer.parseInt(measObjLdn.split("RNC ID=")[1].split("\"")[0]);
                                    networkParameters = String.valueOf(cellIDWithMultiplier) + "|" + String.valueOf(nCellID) + "|" + String.valueOf(targetRncId);
                                    //CELL_TO GERAN icin (4->2)
                                } else {
                                    String targetMCC = measObjLdn.split("Mobile country code=")[1].split("\\,")[0];
                                    String targetMNC = measObjLdn.split("Mobile network code=")[1].split("\\,")[0];
                                    String targetLAC = measObjLdn.split("Location area code=")[1].split("\"")[0];
                                    networkParameters = String.valueOf(cellIDWithMultiplier) + "|" + String.valueOf(nCellID) + "|"
                                            + targetMCC + "|" + targetMNC + "|" + targetLAC;
                                }
                            }
                            break;

                        //nodeb localcell tablolari icin
                        case "NODEB-NUMBER":
                            lteNodebFlag = true;
                            nodebName = measObjLdn.split("\\/")[1].split("\\:")[1].trim();
                            if (nodebName.contains("=")) {
                                nodebName = measObjLdn.split("\\/")[1].split("\\:")[1].split("=")[1].trim();
                            }
                            String rncNeId = nodebNameRncId.get(nodebName);
                            if (rncNeId != null) {
                                String rncNeIdParted = rncNeId.split("000")[rncNeId.split("000").length - 1];
                                rncbscID = Integer.parseInt(rncNeIdParted);
                                networkID = nodebNameToNodeBId.get(nodebName);
                                networkParameters = networkID + "|" + rncNeId;
                            }
                            break;
                        case "BOARD-NUMBER":
                            lteNodebFlag = true;
                            nodebName = measObjLdn.split("\\/")[0];
                            String boardName = measObjLdn.split("\\/")[1].split(":")[1];
                            networkID = enodebNameToENodeBId.get(nodebName);
                            if (networkID != null) {
                                if (tableObject.getTableName().startsWith("M2")) {
                                    networkParameters = networkID + "|" + boardName + "|" + "";
                                } else {
                                    networkParameters = networkID + "|" + boardName;
                                }
                            }
                            break;

                        case "LOCALCELL-NUMBER":
                            lteNodebFlag = true;
                            nodebName = measObjLdn.split("NodeB Function Name=")[1].split(",")[0];
                            rncNeId = nodebNameRncId.get(nodebName);
                            String localCell;
                            if (measObjLdn.contains("Function Name")) {
                                localCell = measObjLdn.split("\\/")[1].split("Local Cell ID=")[1].split("\\,")[0].trim();
                            } else {
                                localCell = measObjLdn.split("\\/")[1].split("\\=")[1].trim();
                            }
                            if (rncNeId != null) {
                                String rncNeIdParted = rncNeId.split("000")[rncNeId.split("000").length - 1];
                                rncbscID = Integer.parseInt(rncNeIdParted);

                                networkID = nodebNameToNodeBId.get(nodebName);
                                String localCellId = networkIdGenerator(rncbscID, Integer.parseInt(localCell), "CELL", vendorID);
                                networkParameters = networkID + "|" + localCellId + "|" + rncNeId;
                            }
                            break;

                        case "NRCELL-NUMBER":
                            networkID = networkIdGenerator(rncbscID, Integer.parseInt(measObjLdn.split(" NR Cell ID=")[1].split(",")[0].trim()), "CELL", 7);
                            networkParameters = networkID;
                            ParserEngine_pm_HW_2g3gcs.cellCounter.put(dataDate + "|" + neName + measObjLdn.split(" NR Cell ID=")[1].split(",")[0].trim(), true);
                            break;

                        case "NRDUCELL-NUMBER":
                            networkID = networkIdGenerator(rncbscID, Integer.parseInt(measObjLdn.split(" NR DU Cell ID=")[1].split(",")[0].trim()), "CELL", 7);
                            networkParameters = networkID;
                            ParserEngine_pm_HW_2g3gcs.cellCounter.put(dataDate + "|" + neName + measObjLdn.split(" NR DU Cell ID=")[1].split(",")[0].trim(), true);
                            break;
                    }
                    break;

                case "HW3G":
                    switch (fss1.getTableType()) {

                        // generating RNC ID as NETWORK_ID
                        case "RNC-NUMBER":
                            networkID = networkIdGenerator(rncbscID, rncbscID, "RNC/BSC", vendorID);
                            networkParameters = networkID;
                            break;

                        // generating measObjLdn string completely as NETWORK_ID (without any splitting)
                        case "MEASOBJLDN-VARCHAR2":
                            networkID = measObjLdn;
                            networkParameters = networkID;
                            break;

                        // generating CELL ID as NETWORK_ID
                        case "CELL-NUMBER":
                            networkID = networkIdGenerator(rncbscID, Integer.parseInt(measObjLdn.split("CellID=")[1]), "CELL", vendorID);
                            networkParameters = networkID;
                            ParserEngine_pm_HW_2g3gcs.cellCounter.put(dataDate + "|" + neName + measObjLdn.split("CellID=")[1], true);
                            break;

                        // generating CELL ID as CELLID and NBR CELL ID as NCELLID
                        case "NCELL-NUMBER":
                            int cellID = Integer.parseInt(measObjLdn.split("CellID=")[1].split("/")[0]);
                            String cellIDWithMultiplier = networkIdGenerator(rncbscID, cellID, "CELL", vendorID);

                            //3->2
                            if (measObjLdn.contains("MCC:")) {
                                String mcc = measObjLdn.split("MCC:")[1].split("/")[0];
                                String mnc = measObjLdn.split("MNC:")[1].split(":")[0];
                                String lac = measObjLdn.split("MNC:")[1].split(":")[1];
                                String ci = measObjLdn.split("MNC:")[1].split(":")[2];
                                networkParameters = String.valueOf(cellIDWithMultiplier) + "|" + mcc + "|" + mnc + "|" + lac + "|" + ci;
                            } else {
                                String splittedMeasObjLdn[] = measObjLdn.split(":");
                                int nCellID = Integer.parseInt(splittedMeasObjLdn[splittedMeasObjLdn.length - 1]);
                                String splittedMeasObjLdn2[] = measObjLdn.split("\\/DEST");
                                int nRncID = Integer.parseInt(splittedMeasObjLdn2[0].split("UMTS:")[1]);

                                networkParameters = String.valueOf(cellIDWithMultiplier) + "|" + String.valueOf(nRncID) + "|" + String.valueOf(nCellID);
                            }

                            break;

                        // generating NODEB ID from OBJECTS_HW3G table
                        case "RNCNODEB-NUMBER":
                            networkID = nodebNameToNodeBId.get(measObjLdn.split("NodeB Name=")[1]);
                            networkParameters = networkID;
                            break;

                        // generating NODEB ID and CNOPINDEX 
                        case "RNCPSTRAFFIC-VARCHAR2":
                            networkID = neNameAndIdList.get(fss1.getNeName());
                            networkID = networkIdGenerator(rncbscID, Integer.parseInt(networkID), "RNC/BSC", vendorID);
                            String nodeID = measObjLdn.split(",")[0].split("CNNodeID=")[1];
                            String opIndex = measObjLdn.split(",")[1].split("CNOPINDEX=")[1];
                            networkParameters = networkID + "|" + nodeID + "|" + opIndex;
                            break;
                    }
                    break;

                case "HW2G":
                    switch (fss1.getTableType()) {
                        // generating BSC ID as NETWORK_ID
                        case "BSC-NUMBER":
                            networkID = networkIdGenerator(rncbscID, rncbscID, "RNC/BSC", vendorID);
                            networkParameters = networkID;
                            break;

                        // generating CELL ID as NETWORK_ID
                        case "CELL-NUMBER":
                            String cellIDFromCGI0[] = (measObjLdn.split("CGI=")[1]).split("\\-");
                            networkID = networkIdGenerator(rncbscID, Integer.parseInt(cellIDFromCGI0[cellIDFromCGI0.length - 1]), "CELL", vendorID);
                            networkParameters = networkID;
                            ParserEngine_pm_HW_2g3gcs.cellCounter.put(dataDate + "|" + neName + cellIDFromCGI0[cellIDFromCGI0.length - 1], true);
                            break;

                        case "BTS-NUMBER":
                            String siteName = measObjLdn.split("SITE:")[1];
                            String btsId = btsNameToBtsId.get(siteName);

                            networkID = btsId;
                            networkParameters = networkID;
                            break;

                        // generating TRX ID as NETWORK_ID
                        case "TRX-NUMBER":
                            String trxIndex = measObjLdn.split("TRX Index=")[1];
                            if (trxIndex.contains(",")) {
                                trxIndex = trxIndex.split("\\,")[0].trim();
                            }
                            networkID = networkIdGenerator(rncbscID, Integer.parseInt(trxIndex), "TRX", vendorID);
                            networkParameters = networkID;
                            break;

                        // generating CELL ID as NETWORK_ID and TARGET CELL ID as TARGET_ID
                        case "HO-NUMBER":
                            String cellIDFromCGI1[] = (measObjLdn.split("CGI=")[1]).split("\\/")[0].split("\\-");
                            String cellID = cellIDFromCGI1[cellIDFromCGI1.length - 1];
                            cellID = networkIdGenerator(rncbscID, Integer.parseInt(cellID), "CELL", vendorID);

                            String targetCellID = "0";
                            String targetNetworkCellID = "0";

                            if (measObjLdn.split("CGI=")[1].contains("/")) {
                                //2->2 HO meas
                                try {
                                    String targetCellIDFromCGI[] = (measObjLdn.split("CGI=")[1]).split("\\/")[1].split("\\-");
                                    String mcc = targetCellIDFromCGI[0].split(":")[1];
                                    String mnc = targetCellIDFromCGI[1];
                                    String lac = targetCellIDFromCGI[2];
                                    String ci = targetCellIDFromCGI[3];
                                    targetCellID = targetCellIDFromCGI[targetCellIDFromCGI.length - 1];
                                    targetNetworkCellID = networkIdGenerator(rncbscID, Integer.parseInt(targetCellID), "CELL", vendorID);
                                    networkParameters = cellID + "|" + mcc + "|" + mnc + "|" + lac + "|" + ci;

                                    //2->3 HO meas
                                } catch (Exception ex) {
                                    targetCellID = (measObjLdn.split("CGI=")[1]).split("\\/")[1].split("\\:")[1];
                                    targetNetworkCellID = (measObjLdn.split("CGI=")[1]).split("\\/")[2].split("\\:")[1];
                                    networkParameters = cellID + "|" + targetCellID + "|" + targetNetworkCellID;
                                }
                            } else if (measObjLdn.contains("CGI=")) {
                                String targetCellIDFromCGI[] = measObjLdn.split("CGI=")[1].split("\\-");
                                String mcc = targetCellIDFromCGI[0];
                                String mnc = targetCellIDFromCGI[1];
                                String lac = targetCellIDFromCGI[2];
                                String ci = targetCellIDFromCGI[3];
                                targetCellID = targetCellIDFromCGI[targetCellIDFromCGI.length - 1];
                                targetNetworkCellID = networkIdGenerator(rncbscID, Integer.parseInt(targetCellID), "CELL", vendorID);
                                networkParameters = cellID + "|" + mcc + "|" + mnc + "|" + lac + "|" + ci;
                            } else {
                                //NO-Target handover
                                networkParameters = cellID;
                            }

                            break;

                        // getting measObjLdn string completely as NETWORK_ID (without any splitting)
                        case "MEASOBJLDN-VARCHAR2":
                            networkID = measObjLdn.replace(" ", "");
                            networkParameters = networkID;
                            break;
                    }
                    break;
            }
            String returnedValueNetworkIdGenerator = "";
            if (systemType.equals("HW2G") || systemType.equals("HW3G")) {
                returnedValueNetworkIdGenerator = "|" + networkIdGenerator(rncbscID, rncbscID, "RNC/BSC", vendorID);
            } else if (systemType.equals("HW4G") && lteNodebFlag && constantColumns.contains("RNCID")) {
            } else if (systemType.equals("HW4G") && !lteNodebFlag) {
                returnedValueNetworkIdGenerator = "|" + networkIdGenerator(rncbscID, rncbscID, "NODEB", vendorID);
            } else if (systemType.equals("HW5G") && !gNodebFlag) {
                returnedValueNetworkIdGenerator = "|" + networkIdGenerator(rncbscID, rncbscID, "NODEB", vendorID);
            }

            if (networkParameters != null) {
                String row = networkParameters
                        + returnedValueNetworkIdGenerator + "|"
                        + fss1.getDataDate() + "|"
                        + String.valueOf(fss1.getCounterValues().get(i)).trim().replace(" ", "|");

                String rowAfterGetValues = CommonLibrary.
                        get_RecordValue(constantColumns + "|" + functionSubSetHeader,
                                row, constantColumns + "|" + variableColumns, "0", "|", "|");
                sb.append(rowAfterGetValues);
                sb.append("\n");
            }

        }

        if (sb.length() != 0) {
            writeIntoFilesWithController(txtFileName, sb.toString().replace("|NIL|", "|0|").replace("|NIL", "|0").replace("NIL|", "0|"));
        }
    }

    private String networkIdGenerator(int parentID, int childID, String neType, int vendorID) {

        int neClassTypeID = 0;
        switch (neType) {
            case "RNC/BSC":
                neClassTypeID = BSCCLASSTYPEID;
                break;
            case "NODEB":
                neClassTypeID = BTSCLASSTYPEID;
                break;
            case "CELL":
                neClassTypeID = CELLCLASSTYPEID;
                break;
            case "TRX":
                neClassTypeID = TRXCLASSTYPEID;
        }

        String result = null;
        BigDecimal bd = new BigDecimal("10");
        BigDecimal resultBigDecimal;
        if (lteNodebFlag) {
            //vendorId hw3g'e cevriliyor lte'de gelen nodeb'ler icin
            resultBigDecimal = bd.pow(21).multiply(BigDecimal.valueOf(4)).
                    add(bd.pow(16).multiply(BigDecimal.valueOf(neClassTypeID))).
                    add(bd.pow(8).multiply(BigDecimal.valueOf(parentID))).
                    add(BigDecimal.valueOf(childID));
        } else {
            resultBigDecimal = bd.pow(21).multiply(BigDecimal.valueOf(vendorID)).
                    add(bd.pow(16).multiply(BigDecimal.valueOf(neClassTypeID))).
                    add(bd.pow(8).multiply(BigDecimal.valueOf(parentID))).
                    add(BigDecimal.valueOf(childID));
        }

        return resultBigDecimal.toString();
    }

}
