package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_cm_HW_2g3g;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ParserIOException;
import com.ttgint.parserEngine.parserHandler.SaxParserHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;

public class HWCmSaxXmlParser2G extends SaxParserHandler implements Runnable {

    private String tempValue;
    private HWCmObjects cmObj;
    private final int neRawID;
    private final String neName;
    private final String dataDate;
    private final HashMap<String, String> mapCellNoTrxId;

    private int moIndex = 0;
    private String className;
    private String attrName;
    private HashMap<String, String> classNameMap = new HashMap<>();
    private String nodeName;

    public HWCmSaxXmlParser2G(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType,
            String neName, int neRawID, String dataDate) {
        super(currentFileProgress, operationSystem, progType);
        this.neRawID = neRawID;
        this.neName = neName;
        this.dataDate = dataDate;
        this.mapCellNoTrxId = new HashMap<>();
    }

    @Override
    public void onStartParseOperation() {
        HWCmObjects cmObjTopNE = new HWCmObjects();
        cmObjTopNE.setDataDate(dataDate);
        cmObjTopNE.setActStatus(true);
        String generatedID = networkIdGenerator(neRawID, neRawID, "RNC/BSC");
        cmObjTopNE.setObjectID(generatedID);
        cmObjTopNE.setObjectName(neName);
        cmObjTopNE.setObjectParentID(generatedID);
        cmObjTopNE.setTopNEID(generatedID);
        cmObjTopNE.setObjectType(ParserEngine_cm_HW_2g3g.BSCCLASSTYPEID);
        writeObjectsIntoFile(cmObjTopNE);
    }

    private boolean classNameFlagForNE = false;
    private boolean neVersionFlag;

    private boolean actStatusFlagForBTS;
    private boolean btsIdFlagForBTS;
    private boolean btsNameFlagBTS;
    private boolean classNameFlagForBTS = false;

    private boolean actStatusFlagForCELL;
    private boolean btsIdFlagForCELL;
    private boolean btsNameFlagCELL;
    private boolean cellIDForCELL;
    private boolean cellNameForCELL;
    private boolean ciForCELL;
    private boolean classNameFlagForCELL = false;

    private boolean actStatusFlagForTRX;
    private boolean cellIDForTRX;
    private boolean trxIDForTRX;
    private boolean trxNameForTRX;
    private boolean classNameFlagForTRX = false;

    @Override
    public void startElement(String s, String s1, String elementName, Attributes attributes) {
        tempValue = "";

        if (elementName.equals("MO")) {
            if (moIndex > 0) { //write first cause of the ne
                if (moIndex == 1) {
                    nodeName = classNameMap.get("name");
                }
                write();
            }
            moIndex++;
            className = attributes.getValue("className");
        } else if (elementName.equals("attr")) {
            attrName = attributes.getValue("name");
        }

        actStatusFlagForBTS = false;
        btsIdFlagForBTS = false;
        btsNameFlagBTS = false;

        actStatusFlagForCELL = false;
        btsIdFlagForCELL = false;
        btsNameFlagCELL = false;
        cellIDForCELL = false;
        cellNameForCELL = false;
        ciForCELL = false;

        actStatusFlagForTRX = false;
        cellIDForTRX = false;
        trxIDForTRX = false;
        trxNameForTRX = false;

        if (elementName.equals("MO")) {
            if (attributes.getValue("className").equals("BSC6900GSMNE")
                    || attributes.getValue("className").equals("BSC6910GSMNE")) {
                classNameFlagForNE = true;
            }

            if (attributes.getValue("className").equals("BSC6900GSMBTS")
                    || attributes.getValue("className").equals("BSC6910GSMBTS")) {
                if (cmObj == null) {
                    cmObj = new HWCmObjects();
                    cmObj.setDataDate(dataDate);
                    cmObj.setTopNEID(networkIdGenerator(neRawID, neRawID, "RNC/BSC"));
                    cmObj.setObjectType(ParserEngine_cm_HW_2g3g.BTSCLASSTYPEID);
                    classNameFlagForBTS = true;
                }
            }

            if (attributes.getValue("className").equals("BSC6900GSMCell")
                    || attributes.getValue("className").equals("BSC6910GSMGCELL")) {
                if (cmObj == null) {
                    cmObj = new HWCmObjects();
                    cmObj.setDataDate(dataDate);
                    cmObj.setTopNEID(networkIdGenerator(neRawID, neRawID, "RNC/BSC"));
                    cmObj.setObjectType(ParserEngine_cm_HW_2g3g.CELLCLASSTYPEID);
                    classNameFlagForCELL = true;
                }
            }

            if (attributes.getValue("className").equals("BSC6900GSMGTRX")
                    || attributes.getValue("className").equals("BSC6910GSMGTRX")) {
                if (cmObj == null) {
                    cmObj = new HWCmObjects();
                    cmObj.setDataDate(dataDate);
                    cmObj.setTopNEID(networkIdGenerator(neRawID, neRawID, "RNC/BSC"));
                    cmObj.setObjectType(ParserEngine_cm_HW_2g3g.TRXCLASSTYPEID);
                    classNameFlagForTRX = true;
                }
            }
        }

        if (elementName.equals("attr") && classNameFlagForNE == true) {
            if (attributes.getValue("name").equals("neVersion")) {
                neVersionFlag = true;
            }
        }

        if (elementName.equals("attr") && classNameFlagForBTS == true) {

            if (attributes.getValue("name").equals("ACTSTATUS")) {
                actStatusFlagForBTS = true;
            }
            if (attributes.getValue("name").equals("BTSID")) {
                btsIdFlagForBTS = true;
            }
            if (attributes.getValue("name").equals("BTSNAME")) {
                btsNameFlagBTS = true;
            }
        }

        if (elementName.equals("attr") && classNameFlagForCELL == true) {

            if (attributes.getValue("name").equals("ACTSTATUS")) {
                actStatusFlagForCELL = true;
            }
            if (attributes.getValue("name").equals("BTSID")) {
                btsIdFlagForCELL = true;
            }
            if (attributes.getValue("name").equals("CELLID")) {
                cellIDForCELL = true;
            }
            if (attributes.getValue("name").equals("CELLNAME")) {
                cellNameForCELL = true;
            }
            if (attributes.getValue("name").equals("CI")) {
                ciForCELL = true;
            }
        }

        if (elementName.equals("attr") && classNameFlagForTRX == true) {

            if (attributes.getValue("name").equals("ACTSTATUS")) {
                actStatusFlagForTRX = true;
            }
            if (attributes.getValue("name").equals("CELLID")) {
                cellIDForTRX = true;
            }
            if (attributes.getValue("name").equals("TRXID")) {
                trxIDForTRX = true;
            }
            if (attributes.getValue("name").equals("name")) {
                trxNameForTRX = true;
            }
        }
    }

    @Override
    public void endElement(String s, String s1, String element) {

        if (element.equals("attr")) {
            classNameMap.put(attrName, tempValue);
            if (tempValue != null && tempValue.contains("=")) {
                for (String item : tempValue.split("\\,")) {
                    try {
                        classNameMap.put(attrName + "." + item.split("\\=")[0].trim(), item.split("\\=")[1].trim());
                    } catch (Exception e) {
                    }
                }
            }
        } else if (element.equals("MOTree")) {//write the last one
            write();
        }

        if (neVersionFlag) {
            String ranVersionName = tempValue.substring(tempValue.indexOf("R"), tempValue.indexOf("ENG"));
            try {
                //Update ranversion
                dbHelper.updateRanVersionHw(AbsParserEngine.operatorName, AbsParserEngine.systemType, neName, ranVersionName);
            } catch (Exception ex) {
            }
            neVersionFlag = false;
        }

        if (actStatusFlagForBTS) {
            if ("ACTIVATED".equals(tempValue)) {
                cmObj.setActStatus(true);
            } else {
                cmObj.setActStatus(false);
            }
        }
        if (btsIdFlagForBTS) {
            cmObj.setObjectID(networkIdGenerator(neRawID, Integer.parseInt(tempValue), "NODEB/BTS"));
            cmObj.setObjectParentID(networkIdGenerator(neRawID, neRawID, "RNC/BSC"));
        }
        if (btsNameFlagBTS) {
            cmObj.setObjectName(tempValue);
            classNameFlagForBTS = false;

            writeObjectsIntoFile(cmObj);

            cmObj = null;
        }

        if (actStatusFlagForCELL) {
            if ("ACTIVATED".equals(tempValue)) {
                cmObj.setActStatus(true);
            } else {
                cmObj.setActStatus(false);
            }
        }
        if (btsIdFlagForCELL) {
            cmObj.setObjectParentID(networkIdGenerator(neRawID, Integer.parseInt(tempValue), "NODEB/BTS"));
        }
        if (cellIDForCELL) {
            cmObj.setTempDescription(networkIdGenerator(neRawID, Integer.parseInt(tempValue), "CELL"));
        }
        if (cellNameForCELL) {
            cmObj.setObjectName(tempValue);
        }
        if (ciForCELL) {
            cmObj.setObjectID(networkIdGenerator(neRawID, Integer.parseInt(tempValue), "CELL"));
            mapCellNoTrxId.put(cmObj.getTempDescription(), cmObj.getObjectID());
            classNameFlagForCELL = false;

            writeObjectsIntoFile(cmObj);

            cmObj = null;
        }

        if (actStatusFlagForTRX) {
            if ("ACTIVATED".equals(tempValue)) {
                cmObj.setActStatus(true);
            } else {
                cmObj.setActStatus(false);
            }
        }
        if (cellIDForTRX) {
            String cellIDInTrxClassInfo = networkIdGenerator(neRawID, Integer.parseInt(tempValue), "CELL");
            cmObj.setObjectParentID(mapCellNoTrxId.get(cellIDInTrxClassInfo));
        }
        if (trxIDForTRX) {
            cmObj.setObjectID(networkIdGenerator(neRawID, Integer.parseInt(tempValue), "TRX"));
        }
        if (trxNameForTRX) {
            cmObj.setObjectName(tempValue);
            classNameFlagForTRX = false;

            writeObjectsIntoFile(cmObj);

            cmObj = null;
        }
    }

    @Override
    public void characters(char[] ac, int i, int j) {
        tempValue += new String(ac, i, j);
    }

    private void writeObjectsIntoFile(HWCmObjects cmO) {
        StringBuilder objRecord = new StringBuilder();
        if (cmO.isActStatus()) {
            //System.out.print(cmO.getDataDate() + "|");
            objRecord.append(cmO.getDataDate()).append("|");

            //System.out.print(cmO.getObjectID() + "|");
            objRecord.append(cmO.getObjectID()).append("|");

            //System.out.print(cmO.getObjectParentID() + "|");
            objRecord.append(cmO.getObjectParentID()).append("|");

            //System.out.print(cmO.getTopNEID() + "|");
            objRecord.append(cmO.getTopNEID()).append("|");

            //System.out.print(cmO.getObjectName() + "|");
            objRecord.append(cmO.getObjectName()).append("|");

            //System.out.println(cmO.getObjectType());
            objRecord.append(cmO.getObjectType()).append("\n");
        }
        String objFile = AbsParserEngine.LOCALFILEPATH + "OBJECTS_HW2G" + AbsParserEngine.integratedFileExtension;
        try {
            writeIntoFilesWithController(objFile, objRecord.toString());
        } catch (ParserIOException ex) {
            Logger.getLogger(HWCmSaxXmlParser2G.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private String networkIdGenerator(int parentID, int childID, String neType) {

        int neClassTypeID = 0;
        switch (neType) {
            case "RNC/BSC":
                neClassTypeID = ParserEngine_cm_HW_2g3g.BSCCLASSTYPEID;
                break;
            case "NODEB/BTS":
                neClassTypeID = ParserEngine_cm_HW_2g3g.BTSCLASSTYPEID;
                break;
            case "CELL":
                neClassTypeID = ParserEngine_cm_HW_2g3g.CELLCLASSTYPEID;
                break;
            case "TRX":
                neClassTypeID = ParserEngine_cm_HW_2g3g.TRXCLASSTYPEID;
        }

        String result = null;
        BigDecimal bd = new BigDecimal("10");
        BigDecimal resultBigDecimal = bd.pow(21).multiply(BigDecimal.valueOf(AbsParserEngine.vendorID)).
                add(bd.pow(16).multiply(BigDecimal.valueOf(neClassTypeID))).
                add(bd.pow(8).multiply(BigDecimal.valueOf(parentID))).
                add(BigDecimal.valueOf(childID));

        return resultBigDecimal.toString();
    }

    @Override
    public void onstopParseOperation() {
    }

    private void write() {
        try {
            String tableName = null;
            try {
                if (className.equals("BSC6900GSMNE") || className.equals("BSC6910GSMNE")) {
                    tableName = "OBJECTS_HW2G_BSC";

                    String generatedID = networkIdGenerator(neRawID, neRawID, "RNC/BSC");
                    classNameMap.put("const_topParentId", generatedID);
                    classNameMap.put("const_parentId", generatedID);
                    classNameMap.put("const_neId", generatedID);
                } else if (className.equals("BSC6900GSMBTS") || className.equals("BSC6910GSMBTS")) {
                    tableName = "OBJECTS_HW2G_BTS";

                    classNameMap.put("const_topParentId", networkIdGenerator(neRawID, neRawID, "RNC/BSC"));
                    classNameMap.put("const_parentId", networkIdGenerator(neRawID, neRawID, "RNC/BSC"));
                    classNameMap.put("const_neId",
                            networkIdGenerator(
                                    neRawID,
                                    Integer.parseInt(classNameMap.get("BTSID")),
                                    "NODEB/BTS"));
                } else if (className.equals("BSC6900GSMCell") || className.equals("BSC6910GSMGCELL")) {
                    tableName = "OBJECTS_HW2G_CELL";

                    classNameMap.put("const_topParentId", networkIdGenerator(neRawID, neRawID, "RNC/BSC"));
                    classNameMap.put("const_parentId",
                            networkIdGenerator(
                                    neRawID,
                                    Integer.parseInt(classNameMap.get("BTSID")),
                                    "NODEB/BTS"));
                    classNameMap.put("const_neId",
                            networkIdGenerator(
                                    neRawID,
                                    Integer.parseInt(classNameMap.get("CI")),
                                    "CELL"));
                } else if (className.equals("BSC6900GSMGTRX") || className.equals("BSC6910GSMGTRX")) {
                    tableName = "OBJECTS_HW2G_TRX";

                    classNameMap.put("const_topParentId", networkIdGenerator(neRawID, neRawID, "RNC/BSC"));
                    classNameMap.put("const_parentId",
                            mapCellNoTrxId.get(
                                    networkIdGenerator(
                                            neRawID,
                                            Integer.parseInt(classNameMap.get("CELLID")),
                                            "CELL")
                            )
                    );
                    classNameMap.put("const_neId",
                            networkIdGenerator(
                                    neRawID,
                                    Integer.parseInt(classNameMap.get("TRXID")),
                                    "TRX"));
                }
            } catch (Exception e) {
            }

            if (tableName != null) {
                RawTableObject tableObject = TableWatcher.getInstance().getTableObjectFromTableName(tableName);
                if (tableObject != null) {
                    classNameMap.put("const_dataDate", dataDate);
                    classNameMap.put("const_bscName", nodeName);
                    StringBuilder builderKey = new StringBuilder();
                    StringBuilder builderValue = new StringBuilder();
                    for (String key : classNameMap.keySet()) {
                        builderKey.append(key).append(AbsParserEngine.resultParameter);
                        builderValue.append(classNameMap.get(key)).append(AbsParserEngine.resultParameter);
                    }

                    String record
                            = CommonLibrary.get_RecordValue(
                                    builderKey.toString().substring(0, builderKey.toString().length() - 1),
                                    builderValue.toString().substring(0, builderValue.toString().length() - 1),
                                    tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter),
                                    "",
                                    AbsParserEngine.resultParameter,
                                    AbsParserEngine.resultParameter);

                    writeIntoFilesWithController(
                            AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension,
                            record.replace(AbsParserEngine.resultParameter + "null" + AbsParserEngine.resultParameter, "") + "\n");
                }
            }
        } catch (ParserIOException ex) {
        }
        classNameMap.clear();
    }
}
