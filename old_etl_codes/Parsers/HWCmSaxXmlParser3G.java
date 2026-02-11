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

public class HWCmSaxXmlParser3G extends SaxParserHandler implements Runnable {

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

    private ArrayList<HashMap<String, String>> rawDataRnc = new ArrayList<>();
    private ArrayList<HashMap<String, String>> rawDataNodeB = new ArrayList<>();
    private ArrayList<HashMap<String, String>> rawDataCell = new ArrayList<>();

    public HWCmSaxXmlParser3G(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType, String neName, int neRawID, String dataDate) {
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
        String generatedID = networkIdGenerator(neRawID, neRawID, "RNC/BSC");
        cmObjTopNE.setObjectID(generatedID);
        cmObjTopNE.setObjectName(neName);
        cmObjTopNE.setObjectParentID(generatedID);
        cmObjTopNE.setTopNEID(generatedID);
        cmObjTopNE.setObjectType(
                ParserEngine_cm_HW_2g3g.BSCCLASSTYPEID);
        writeObjectsIntoFile(cmObjTopNE);
    }

    private boolean blkStatusForNODEB;  // blk stands for blocked
    private boolean nodebIdForNODEB;
    private boolean nodebNameForNODEB;
    private boolean classNameForNODEB = false;
    private boolean neVersionFlag;
    private boolean classNameFlagForNE = false;

    private boolean actStatusFlagForCELL;
    private boolean nodebNameForCELL;
    private boolean cellIDForCELL;
    private boolean cellNameForCELL;
    private boolean classNameFlagForCELL = false;

    @Override
    public void startElement(String s, String s1, String elementName, Attributes attributes) {
        tempValue = "";

        if (elementName.equals("MO")) {
            if (moIndex > 0) { //write first cause of the ne
                if (moIndex == 1) {
                    nodeName = classNameMap.get("name");
                }
                collect();
            }
            moIndex++;
            className = attributes.getValue("className");
        } else if (elementName.equals("attr")) {
            attrName = attributes.getValue("name");
        }

        blkStatusForNODEB = false;
        nodebIdForNODEB = false;
        nodebNameForNODEB = false;

        actStatusFlagForCELL = false;
        nodebNameForCELL = false;
        cellIDForCELL = false;
        cellNameForCELL = false;

        if (elementName.equals("MO")) {
            if (attributes.getValue("className").equals("BSC6900UMTSNE")
                    || attributes.getValue("className").equals("BSC6910UMTSNE")) {
                classNameFlagForNE = true;
            }

            if (attributes.getValue("className").equals("BSC6900UMTSNODEB")
                    || attributes.getValue("className").equals("BSC6910UMTSNODEB")) {
                if (cmObj == null) {
                    cmObj = new HWCmObjects();
                    //Nodeb'ler her zaman aktif alinir cell'ler deactive olunca gozukmez zaten
                    cmObj.setActStatus(true);
                    cmObj.setDataDate(dataDate);
                    cmObj.setTopNEID(networkIdGenerator(neRawID, neRawID, "RNC/BSC"));
                    cmObj.setObjectType(ParserEngine_cm_HW_2g3g.BTSCLASSTYPEID);
                    classNameForNODEB = true;
                }
            }

            if (attributes.getValue("className").equals("BSC6900UMTSCell")
                    || attributes.getValue("className").equals("BSC6910UMTSUCELL")) {
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

        if (elementName.equals("attr") && classNameForNODEB == true) {

            if (attributes.getValue("name").equals("BLKSTATUS")) {
                blkStatusForNODEB = true;
            }
            if (attributes.getValue("name").equals("NODEBID")) {
                nodebIdForNODEB = true;
            }
            if (attributes.getValue("name").equals("NODEBNAME")) {
                nodebNameForNODEB = true;
            }
        }

        if (elementName.equals("attr") && classNameFlagForCELL == true) {

            if (attributes.getValue("name").equals("ACTSTATUS")) {
                actStatusFlagForCELL = true;
            }
            if (attributes.getValue("name").equals("CELLID")) {
                cellIDForCELL = true;
            }
            if (attributes.getValue("name").equals("CELLNAME")) {
                cellNameForCELL = true;
            }
            if (attributes.getValue("name").equals("NODEBNAME")) {
                nodebNameForCELL = true;
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
            collect();

            writeAll();
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

        if (blkStatusForNODEB) {
            if ("UNBLOCKED".equals(tempValue)) {
                cmObj.setActStatus(true);
            } else {
                cmObj.setActStatus(true);
            }
        }
        if (nodebIdForNODEB) {
            cmObj.setObjectID(networkIdGenerator(neRawID, Integer.parseInt(tempValue), "NODEB/BTS"));
            cmObj.setObjectParentID(networkIdGenerator(neRawID, neRawID, "RNC/BSC"));
        }
        if (nodebNameForNODEB) {
            cmObj.setObjectName(tempValue);
            mapNodebNameNodebId.put(cmObj.getObjectName(), cmObj.getObjectID());
            classNameForNODEB = false;
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
        if (cellIDForCELL) {
            cmObj.setObjectID(networkIdGenerator(neRawID, Integer.parseInt(tempValue), "CELL"));
        }
        if (cellNameForCELL) {
            cmObj.setObjectName(tempValue);
        }
        if (nodebNameForCELL) {
            cmObj.setObjectParentID(tempValue);
            if (cmObj.isActStatus()) {
                cmCellObjects.add(cmObj);
            }
            classNameFlagForCELL = false;
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
        String fileName = AbsParserEngine.LOCALFILEPATH + "OBJECTS_HW3G" + AbsParserEngine.integratedFileExtension;

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
            cmCellObj.setObjectParentID(mapNodebNameNodebId.get(cmCellObj.getObjectParentID()));
            writeObjectsIntoFile(cmCellObj);
            cmCellObj = null;
        }

    }

    private void collect() {

        if (className.equals("BSC6900UMTSNE") || className.equals("BSC6910UMTSNE")) {
            rawDataRnc.add(classNameMap);
        } else if (className.equals("BSC6900UMTSNODEB") || className.equals("BSC6910UMTSNODEB")) {
            rawDataNodeB.add(classNameMap);
        } else if (className.equals("BSC6900UMTSCell") || className.equals("BSC6910UMTSUCELL")) {
            rawDataCell.add(classNameMap);
        } else {
            classNameMap.clear();
        }

        classNameMap = new HashMap<>();
    }

    private void writeAll() {
        write("OBJECTS_HW3G_RNC", rawDataRnc);
        write("OBJECTS_HW3G_NODEB", rawDataNodeB);
        write("OBJECTS_HW3G_CELL", rawDataCell);
    }

    private void write(String tableName, ArrayList<HashMap<String, String>> rawData) {
        try {
            RawTableObject tableObject = TableWatcher.getInstance().getTableObjectFromTableName(tableName);
            if (tableObject != null) {
                String dbColumns = tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter);
                StringBuilder builderRecord = new StringBuilder();
                for (HashMap<String, String> classNames : rawData) {
                    try {
                        if (tableName.equals("OBJECTS_HW3G_RNC")) {
                            String generatedID = networkIdGenerator(neRawID, neRawID, "RNC/BSC");
                            classNames.put("const_topParentId", generatedID);
                            classNames.put("const_parentId", generatedID);
                            classNames.put("const_neId", generatedID);
                        } else if (tableName.equals("OBJECTS_HW3G_NODEB")) {
                            classNames.put("const_topParentId", networkIdGenerator(neRawID, neRawID, "RNC/BSC"));
                            classNames.put("const_parentId", networkIdGenerator(neRawID, neRawID, "RNC/BSC"));
                            classNames.put("const_neId",
                                    networkIdGenerator(
                                            neRawID,
                                            Integer.parseInt(classNames.get("NODEBID")),
                                            "NODEB/BTS"));
                        } else if (tableName.equals("OBJECTS_HW3G_CELL")) {
                            classNames.put("const_topParentId", networkIdGenerator(neRawID, neRawID, "RNC/BSC"));
                            classNames.put("const_parentId",
                                    mapNodebNameNodebId.get(
                                            classNames.get("NODEBNAME")
                                    )
                            );
                            classNames.put("const_neId",
                                    networkIdGenerator(
                                            neRawID,
                                            Integer.parseInt(classNames.get("CELLID")),
                                            "CELL"));
                        }
                    } catch (Exception e) {
                    }

                    classNames.put("const_dataDate", dataDate);
                    classNames.put("const_rncName", nodeName);
                    StringBuilder builderKey = new StringBuilder();
                    StringBuilder builderValue = new StringBuilder();
                    for (String key : classNames.keySet()) {
                        builderKey.append(key).append(AbsParserEngine.resultParameter);
                        builderValue.append(classNames.get(key)).append(AbsParserEngine.resultParameter);
                    }

                    builderRecord
                            .append(
                                    CommonLibrary.get_RecordValue(
                                            builderKey.toString().substring(0, builderKey.toString().length() - 1),
                                            builderValue.toString().substring(0, builderValue.toString().length() - 1),
                                            dbColumns,
                                            "",
                                            AbsParserEngine.resultParameter,
                                            AbsParserEngine.resultParameter)
                                            .replace(AbsParserEngine.resultParameter + "null" + AbsParserEngine.resultParameter, ""))
                            .append("\n");
                }

                writeIntoFilesWithController(
                        AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension,
                        builderRecord.toString());
            }
        } catch (ParserIOException ex) {
        }
    }
}
