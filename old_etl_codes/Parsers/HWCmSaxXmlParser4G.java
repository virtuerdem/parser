package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_cm_HW_2g3g;
import com.ttgint.parserEngine.common.AbsParserEngine;
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

public class HWCmSaxXmlParser4G extends SaxParserHandler implements Runnable {

    private String tempValue;
    private HWCmObjects cmObj;
    private final ArrayList<HWCmObjects> cmCellObjects;
    private final int neRawID;
    private final String neName;
    private final String dataDate;

    private int moIndex = 0;
    private String className;
    private String attrName;
    private HashMap<String, String> classNameMap = new HashMap<>();
    private String nodeName;

    private ArrayList<HashMap<String, String>> rawDataENodeB = new ArrayList<>();
    private ArrayList<HashMap<String, String>> rawDataCell = new ArrayList<>();

    public HWCmSaxXmlParser4G(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType, String neName, int neRawID, String dataDate) {
        super(currentFileProgress, operationSystem, progType);
        this.neRawID = neRawID;
        this.neName = neName;
        this.dataDate = dataDate;
        cmCellObjects = new ArrayList();

    }

    @Override
    public void onStartParseOperation() {
        HWCmObjects cmObjTopNE = new HWCmObjects();
        cmObjTopNE.setDataDate(dataDate);
        cmObjTopNE.setActStatus(true);
        String generatedID = networkIdGenerator(neRawID, neRawID, "NODEB/BTS", AbsParserEngine.vendorID);
        cmObjTopNE.setObjectID(generatedID);
        cmObjTopNE.setObjectName(neName);
        cmObjTopNE.setObjectParentID(generatedID);
        cmObjTopNE.setTopNEID(generatedID);
        cmObjTopNE.setObjectType(
                ParserEngine_cm_HW_2g3g.BTSCLASSTYPEID);
        writeObjectsIntoFile(cmObjTopNE);
    }

    private boolean actStatusFlagForCELL;
    private boolean cellIDForCELL;
    private boolean nrCellIDForCELL;
    private boolean cellNameForCELL;
    private boolean classNameFlagForCELL = false;

    @Override
    public void startElement(String s, String s1, String elementName, Attributes attributes) {
        tempValue = "";

//        if (elementName.equals("MO")) {
//            if (moIndex > 0) { //write first cause of the ne
//                if (moIndex == 1) {
//                    nodeName = classNameMap.get("name");
//                }
//                collect();
//            }
//            moIndex++;
//            className = attributes.getValue("className");
//        } else if (elementName.equals("attr")) {
//            attrName = attributes.getValue("name");
//        }
        actStatusFlagForCELL = false;
        cellIDForCELL = false;
        nrCellIDForCELL = false;
        cellNameForCELL = false;

        if (elementName.equals("MO")) {
            if (attributes.getValue("className").equals("BTS3900CELL") || attributes.getValue("className").equals("NRCELL")) {
                if (cmObj == null) {
                    cmObj = new HWCmObjects();
                    cmObj.setDataDate(dataDate);
                    cmObj.setTopNEID(networkIdGenerator(neRawID, neRawID, "RNC/BSC", AbsParserEngine.vendorID));
                    cmObj.setObjectType(ParserEngine_cm_HW_2g3g.CELLCLASSTYPEID);
                    classNameFlagForCELL = true;
                }
            }
        }

        if (elementName.equals("attr") && classNameFlagForCELL == true) {

            if (attributes.getValue("name").equals("CELLACTIVESTATE")) {
                actStatusFlagForCELL = true;
            }
            if (attributes.getValue("name").equals("LOCALCELLID")) {
                cellIDForCELL = true;
            }
            if (attributes.getValue("name").equals("NRCELLID")) {
                nrCellIDForCELL = true;
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

//        if (element.equals("attr")) {
//            classNameMap.put(attrName, tempValue);
//            if (tempValue != null && tempValue.contains("=")) {
//                for (String item : tempValue.split("\\,")) {
//                    try {
//                        classNameMap.put(attrName + "." + item.split("\\=")[0].trim(), item.split("\\=")[1].trim());
//                    } catch (Exception e) {
//                    }
//                }
//            }
//        } else if (element.equals("MOTree")) {//write the last one
//            collect();
//
//            writeAll();
//        }
        if (actStatusFlagForCELL) {
            if ("CELL_ACTIVE".equals(tempValue)) {
                cmObj.setActStatus(true);
            } else {
                cmObj.setActStatus(false);
            }
        }
        if (cellIDForCELL) {
            cmObj.setObjectID(networkIdGenerator(neRawID, Integer.parseInt(tempValue), "CELL", AbsParserEngine.vendorID));
        }
        if (nrCellIDForCELL) {
            cmObj.setObjectID(networkIdGenerator(neRawID, Integer.parseInt(tempValue), "CELL", 7));
        }
        if (cellNameForCELL) {
            cmObj.setObjectName(tempValue.split("Cell Name=")[1].split(",")[0]);
        }
        if (cellNameForCELL) {
            cmObj.setObjectParentID(networkIdGenerator(neRawID, neRawID, "NODEB/BTS", AbsParserEngine.vendorID));
            if (cmObj.isActStatus()) {
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
        String fileName = AbsParserEngine.LOCALFILEPATH + "OBJECTS_HW4G" + AbsParserEngine.integratedFileExtension;

        try {
            writeIntoFilesWithController(fileName, objRecord.toString());
        } catch (ParserIOException ex) {

        }
    }

    private String networkIdGenerator(int parentID, int childID, String neType, int vendorID) {

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

        BigDecimal bd = new BigDecimal("10");
        BigDecimal resultBigDecimal = bd.pow(21).multiply(BigDecimal.valueOf(vendorID)).
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

    private void collect() {

        if (className.equals("BTS3900NE")) {
            rawDataENodeB.add(classNameMap);
        } else if (className.equals("BTS3900CELL") || className.equals("NRCELL")) {
            rawDataCell.add(classNameMap);
        } else {
            classNameMap.clear();
        }

        classNameMap = new HashMap<>();
    }

    private void writeAll() {
        write("OBJECTS_HW4G_ENODEB", rawDataENodeB);
        write("OBJECTS_HW4G_CELL", rawDataCell);
    }

    private void write(String tableName, ArrayList<HashMap<String, String>> rawData) {
        try {
            RawTableObject tableObject = TableWatcher.getInstance().getTableObjectFromTableName(tableName);
            if (tableObject != null) {
                String dbColumns = tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter);
                StringBuilder builderRecord = new StringBuilder();
                for (HashMap<String, String> classNames : rawData) {
                    try {
                        if (tableName.equals("OBJECTS_HW4G_ENODEB")) {
                            String generatedID = networkIdGenerator(neRawID, neRawID, "NODEB/BTS", AbsParserEngine.vendorID);
                            classNames.put("const_topParentId", generatedID);
                            classNames.put("const_parentId", generatedID);
                            classNames.put("const_neId", generatedID);
                        } else if (tableName.equals("OBJECTS_HW4G_CELL")) {
                            classNames.put("const_topParentId", networkIdGenerator(neRawID, neRawID, "NODEB/BTS", AbsParserEngine.vendorID));
                            classNames.put("const_parentId", networkIdGenerator(neRawID, neRawID, "NODEB/BTS", AbsParserEngine.vendorID));
                            if (classNames.containsKey("LOCALCELLID")) {
                                classNames.put("const_neId",
                                        networkIdGenerator(
                                                neRawID,
                                                Integer.parseInt(classNameMap.get("LOCALCELLID")),
                                                "CELL",
                                                AbsParserEngine.vendorID)
                                );
                            } else if (classNames.containsKey("NRCELLID")) {
                                classNames.put("const_neId",
                                        networkIdGenerator(
                                                neRawID,
                                                Integer.parseInt(classNameMap.get("NRCELLID")),
                                                "CELL",
                                                7)
                                );
                            }
                        }
                    } catch (Exception e) {
                    }

                    classNames.put("const_dataDate", dataDate);
                    classNames.put("const_enodebName", nodeName);
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
