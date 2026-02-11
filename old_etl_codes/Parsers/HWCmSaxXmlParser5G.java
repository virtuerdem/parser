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
import java.util.ArrayList;
import java.util.HashMap;
import org.xml.sax.Attributes;

public class HWCmSaxXmlParser5G extends SaxParserHandler implements Runnable {

    private String tempValue;
    private HWCmObjects cmObj;
    private final ArrayList<HWCmObjects> cmCellObjects;
    private final int neRawID;
    private final String neName;
    private final String dataDate;
    private final HashMap<String, String> mapNodebNameNodebId;

    private int moIndex = 0;
    private String className;
    private String attrName;
    private HashMap<String, String> classNameMap = new HashMap<>();
    private String nodeName;

    public HWCmSaxXmlParser5G(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType, String neName, int neRawID, String dataDate) {
        super(currentFileProgress, operationSystem, progType);
        this.neRawID = neRawID;
        this.neName = neName;
        this.dataDate = dataDate;
        mapNodebNameNodebId = new HashMap<>();
        cmCellObjects = new ArrayList();

    }

    @Override
    public void onStartParseOperation() {
        HWCmObjects cmObjTopNE = new HWCmObjects();
        cmObjTopNE.setDataDate(dataDate);
        cmObjTopNE.setActStatus(true);
        String generatedID = networkIdGenerator(neRawID, neRawID, "NODEB/BTS");
        cmObjTopNE.setObjectID(generatedID);
        cmObjTopNE.setObjectName(neName);
        cmObjTopNE.setObjectParentID(generatedID);
        cmObjTopNE.setTopNEID(generatedID);
        cmObjTopNE.setObjectType(
                ParserEngine_cm_HW_2g3g.BTSCLASSTYPEID);
        writeObjectsIntoFile(cmObjTopNE);
    }

    private boolean neVersionFlag;
    private boolean actStatusFlagForCELL;
    private boolean nodeNameForCELL;
    private boolean cellIDForCELL;
    private boolean cellNameForCELL;
    private boolean classNameFlagForCELL = false;
    private boolean classNameFlagForNE = false;

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

        actStatusFlagForCELL = false;
        nodeNameForCELL = false;
        cellIDForCELL = false;
        cellNameForCELL = false;

        if (elementName.equals("MO")) {
            if (attributes.getValue("className").equals("BTS3900NE")) {
                classNameFlagForNE = true;
            }

            if (attributes.getValue("className").equals("NRCELL")) {
                if (cmObj == null) {
                    cmObj = new HWCmObjects();
                    cmObj.setDataDate(dataDate);
                    cmObj.setTopNEID(networkIdGenerator(neRawID, neRawID, "RNC/BSC"));
                    cmObj.setObjectType(ParserEngine_cm_HW_2g3g.CELLCLASSTYPEID);
                    classNameFlagForCELL = true;
                }
            }
        }

        if (elementName.equals("attr") && classNameFlagForNE == true) {
            if (attributes.getValue("name").equals("neVersion")) {
                neVersionFlag = true;
            }
        }

        if (elementName.equals("attr") && classNameFlagForCELL == true) {

            if (attributes.getValue("name").equals("CELLACTIVESTATE")) {
                actStatusFlagForCELL = true;
            }
            if (attributes.getValue("name").equals("NRCELLID")) {
                cellIDForCELL = true;
            }
            if (attributes.getValue("name").equals("GNODEBFUNCTIONNAME")) {
                nodeNameForCELL = true;
            }
            if (attributes.getValue("name").equals("name")) {
                cellNameForCELL = true;
            }

        }

    }

    @Override
    public void characters(char[] ac, int i, int j) {
        tempValue += new String(ac, i, j);
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
            String ranVersionName = tempValue.substring(tempValue.indexOf("R"), tempValue.indexOf("C"));
            try {
                //Update ranversion
                dbHelper.updateRanVersionHw(AbsParserEngine.operatorName, AbsParserEngine.systemType, neName, ranVersionName);
            } catch (Exception ex) {
            }
            neVersionFlag = false;
        }

        if (actStatusFlagForCELL) {
            if ("CELL_ACTIVE".equals(tempValue)) {
                cmObj.setActStatus(true);
            } else {
                cmObj.setActStatus(false);
            }
        }
        if (cellIDForCELL) {
            cmObj.setObjectID(networkIdGenerator(neRawID, Integer.parseInt(tempValue), "CELL"));
        }
        if (cellNameForCELL) {
            cmObj.setObjectName(tempValue.split("Cell Name=")[1].split(",")[0]);
        }
        if (cellNameForCELL) {
            cmObj.setObjectParentID(networkIdGenerator(neRawID, neRawID, "NODEB/BTS"));
            if (cmObj.isActStatus()
                    && (cmObj.getObjectType() == 30
                    || (cmObj.getObjectType() == 37 && cmObj.getObjectName().startsWith("N")))) {
                cmCellObjects.add(cmObj);
            }
            classNameFlagForCELL = false;
            cmObj = null;
        }
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
        String fileName = AbsParserEngine.LOCALFILEPATH + "OBJECTS_HW5G" + AbsParserEngine.integratedFileExtension;

        try {
            writeIntoFilesWithController(fileName, objRecord.toString());
        } catch (ParserIOException ex) {

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

        for (HWCmObjects cmCellObj : cmCellObjects) {
            writeObjectsIntoFile(cmCellObj);
            cmCellObj = null;
        }

    }

    private void write() {
        try {
            String tableName = null;
            try {
                if (className.equals("BTS3900NE")) {
                    tableName = "OBJECTS_HW5G_GNODEB";

                    String generatedID = networkIdGenerator(neRawID, neRawID, "NODEB/BTS");
                    classNameMap.put("const_topParentId", generatedID);
                    classNameMap.put("const_parentId", generatedID);
                    classNameMap.put("const_neId", generatedID);
                } else if (className.equals("NRCELL")) {
                    tableName = "OBJECTS_HW5G_CELL";

                    classNameMap.put("const_topParentId", networkIdGenerator(neRawID, neRawID, "NODEB/BTS"));
                    classNameMap.put("const_parentId", networkIdGenerator(neRawID, neRawID, "NODEB/BTS"));
                    classNameMap.put("const_neId",
                            networkIdGenerator(
                                    neRawID,
                                    Integer.parseInt(classNameMap.get("NRCELLID")),
                                    "CELL")
                    );
                }
            } catch (Exception e) {
            }

            if (tableName != null) {
                RawTableObject tableObject = TableWatcher.getInstance().getTableObjectFromTableName(tableName);
                if (tableObject != null) {
                    classNameMap.put("const_dataDate", dataDate);
                    classNameMap.put("const_gnodebName", nodeName);
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
