/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.io.File;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_csv_Samsung;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.RawCounterObject;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.exceptions.ParserIOException;
import com.ttgint.parserEngine.parserHandler.FileReaderHandler;
import com.ttgint.parserEngine.parserHandler.SyncOutputController;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import com.ttgint.parserEngine.systemProperties.RanElementsInfo;

/**
 *
 * @author TTGETERZI
 */
public class SamsungPmCsvParserHandler extends FileReaderHandler {

    private String version = "v3.1.0";
    private final ArrayList<String> headerOnList;
    private List<RawCounterObject> counterListFromDb;
    private List<RawCounterObject> constantCounterListFromDb;
    private List<RawCounterObject> variableCounterListFromDb;
    private String headerFromDb;
    private String header;
    private String[] headerSplitted;
    private String familyName;
    private RawTableObject tableObject;
    private String systemType;
    private int vendorId;
    private String ipAdress;
    private Date fileDate;
    private SimpleDateFormat dateFormatterfromFile = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat dateFormattertoResult = new SimpleDateFormat("yyyyMMddHHmm");

    public SamsungPmCsvParserHandler(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
        this.headerOnList = new ArrayList<>();
    }

    @Override
    public void onStartParseOperation() {
        ipAdress = currentFileProgress.getName().split("\\+")[0].trim();

    }

    private boolean isValuesStarted;

    @Override
    public void lineProgress(String line) {
        if (line.startsWith("#family:")) {
            familyName = line.replace("#family:", "").trim();

            while (familyName.contains(",")) {
                familyName = familyName.replace(",", "");
            }
            tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(familyName);
            ParserEngine_pm_csv_Samsung.familiyList.add(familyName);
            if (tableObject == null) {
                requestStop();
                currentFileProgress.delete();
            } else {
                if (tableObject.getTableName().contains("SAM3G")) {
                    vendorId = 31;
                    systemType = "SAM3G";
                } else if (tableObject.getTableName().contains("SAM2G")) {
                    systemType = "SAM2G";
                    vendorId = 30;
                } else {
                    throw new RuntimeException();
                }

            }

        } else if (line.startsWith("#attribute:")) {
            header = line.replace("#attribute:", "").trim();
            headerFromDb = tableObject.getFullColumnOrderUsingCounterNameFil(",");
            counterListFromDb = tableObject.getCounterObjectList();
            constantCounterListFromDb = tableObject.getConstantObjectList();
            variableCounterListFromDb = tableObject.getVariableObjectList();
            headerSplitted = header.split("\\,");
            headerOnList.addAll(Arrays.asList(headerSplitted));
            isValuesStarted = true;
            return;
        }
        if (isValuesStarted) {
            parseLine(line);
            counter++;
            if (counter == 1) {
                //         isValuesStarted = false;
            }
        }

    }
    private int counter = 0;

    private final static int neIdOrder = 0;
    private final static int systemIdOrder = 1;
    private final static int initDateOrder = 3;
    private final static int gpOrder = 4;
    private final static int neNameOrder = 2;
    private final static int locationInfoOrder = 5;

    private final ArrayList<SamsungPmSubset_v2> subsetList = new ArrayList<>();

    SamsungPmSubset_v2 currentSubset;

    @Override
    public void onstopParseOperation() {
        //   System.out.println(subsetList.size());
        for (SamsungPmSubset_v2 each : subsetList) {
            writeIntoFiles(each);
//            break;
        }

    }

    private void parseLine(String line) {
        switch (systemType) {
            case "SAM2G":
                sam2gLineOperation(line);
                break;
            case "SAM3G":
                sam3gLineOperation(line);
                break;
        }

    }

    private void sam2gLineOperation(String line) {
        switch (tableObject.getNeType()) {
            case "SAM_BSC":
                sam2gBscLineOperation(line);
                break;
            case "SAM_BTS":
                sam2gBtsLineOperation(line);
                break;
        }

    }

    private void sam2gBscLineOperation(String line) {
        String[] splittedLine = line.split("\\,");
        String initDate = splittedLine[initDateOrder];
        switch (tableObject.getTableType()) {
            case "Cell":
            case "Trx":
                //top ne id db den dinamik alinacak file daki name den gelicek
                String neName = splittedLine[neNameOrder];
                String rawTopNeId = ParserEngine_pm_csv_Samsung.neNameToIdList.get(neName);
                if (rawTopNeId == null) {
                    return;
                }
                String rawCellId = getCellId(splittedLine[locationInfoOrder]);

                //String rawBaseStationId = getManagedElementBsId(splittedLine[locationInfoOrder]);
                String generatedParentId = generatedId(Integer.parseInt(rawTopNeId),
                        Integer.parseInt(rawTopNeId), vendorId, RanElementsInfo.BSCorRNC.getNeTypeId());
                String generatedNetworkId = generatedId(Integer.parseInt(rawTopNeId),
                        Integer.parseInt(rawCellId), vendorId, RanElementsInfo.CELL.getNeTypeId());

                if (tableObject.getNetworkIdType().equals("AGG_ON")) {
                    currentSubset = findSubsetByUsingNetworkIdAndInitDate(generatedNetworkId, initDate);
                } else {
                    currentSubset = null;
                }

                if (currentSubset == null) {
                    currentSubset = new SamsungPmSubset_v2(headerOnList);
                    constantOperation(generatedNetworkId, initDate, generatedParentId, splittedLine[locationInfoOrder], splittedLine[gpOrder]);

                    for (RawCounterObject each : variableCounterListFromDb) {
                        //    System.out.print(each.getCounterNameFile() + " ");
                        int counterIndex = headerOnList.indexOf(each.getCounterNameFile());
//                        System.out.print(each.getCounterNameFile()+" : ");
//                        System.out.println(splittedLine[counterIndex] + " " + counterIndex);
                        currentSubset.putCounters(each, Double.parseDouble(splittedLine[counterIndex]));

                    }
                    //her counter group value eklemesi tamamlaninca counter 1 arttirlir.
                    currentSubset.incareseSize();
                    currentSubset.setInitDate(initDate);
                    currentSubset.setNetworkId(generatedNetworkId);
                    if (tableObject.getNetworkIdType().equals("AGG_ON")) {
                        subsetList.add(currentSubset);
                    } else {
                        //agregate olmayan direk isleme sokulur
                        writeIntoFiles(currentSubset);
                    }
                    currentSubset = null;
                } else {
                    for (RawCounterObject each : variableCounterListFromDb) {
                        //    System.out.print(each.getCounterNameFile() + " ");
                        int counterIndex = headerOnList.indexOf(each.getCounterNameFile());
                        currentSubset.aggValue(each, Double.parseDouble(splittedLine[counterIndex]));
                    }
                    currentSubset.incareseSize();
                }
                break;
            case "HandoverOU_2G":
                neName = splittedLine[neNameOrder];
                rawTopNeId = ParserEngine_pm_csv_Samsung.neNameToIdList.get(neName);
                if (rawTopNeId == null) {
                    return;
                }
                String ServingCellIdentity = getServingCellIdentity(splittedLine[locationInfoOrder]);
                generatedParentId = generatedId(Integer.parseInt(rawTopNeId),
                        Integer.parseInt(rawTopNeId), vendorId, RanElementsInfo.BSCorRNC.getNeTypeId());
                generatedNetworkId = generatedId(Integer.parseInt(rawTopNeId),
                        Integer.parseInt(ServingCellIdentity), vendorId, RanElementsInfo.CELL.getNeTypeId());

                if (tableObject.getNetworkIdType().equals("AGG_ON")) {
                    currentSubset = findSubsetByUsingNetworkIdAndInitDate(generatedNetworkId, initDate);
                } else {
                    currentSubset = null;
                }
                if (currentSubset == null) {
                    currentSubset = new SamsungPmSubset_v2(headerOnList);
                    constantOperation(generatedNetworkId, initDate, generatedParentId, splittedLine[locationInfoOrder], splittedLine[gpOrder]);

                    for (RawCounterObject each : variableCounterListFromDb) {
                        //    System.out.print(each.getCounterNameFile() + " ");
                        int counterIndex = headerOnList.indexOf(each.getCounterNameFile());
                        currentSubset.putCounters(each, Double.parseDouble(splittedLine[counterIndex]));
                    }
                    //her counter group value eklemesi tamamlaninca counter 1 arttirlir.
                    currentSubset.incareseSize();
                    currentSubset.setInitDate(initDate);
                    currentSubset.setNetworkId(generatedNetworkId);
                    if (tableObject.getNetworkIdType().equals("AGG_ON")) {
                        subsetList.add(currentSubset);
                    } else {
                        //agregate olmayan direk isleme sokulur
                        writeIntoFiles(currentSubset);
                    }
                    currentSubset = null;
                } else {
                    for (RawCounterObject each : variableCounterListFromDb) {
                        //    System.out.print(each.getCounterNameFile() + " ");
                        int counterIndex = headerOnList.indexOf(each.getCounterNameFile());
                        currentSubset.aggValue(each, Double.parseDouble(splittedLine[counterIndex]));
                    }
                    currentSubset.incareseSize();
                }
                break;
            case "HandoverIN_2G":
                neName = splittedLine[neNameOrder];
                rawTopNeId = ParserEngine_pm_csv_Samsung.neNameToIdList.get(neName);
                if (rawTopNeId == null) {
                    return;
                }
                String TargetCellIdentity = getTargetCellIdentity(splittedLine[locationInfoOrder]);
                generatedParentId = generatedId(Integer.parseInt(rawTopNeId),
                        Integer.parseInt(rawTopNeId), vendorId, RanElementsInfo.BSCorRNC.getNeTypeId());
                generatedNetworkId = null;
                try {
                    generatedNetworkId = generatedId(Integer.parseInt(rawTopNeId),
                            Integer.parseInt(TargetCellIdentity), vendorId, RanElementsInfo.CELL.getNeTypeId());
                } catch (Exception e) {
                    System.out.println(rawTopNeId);
                    System.out.println(TargetCellIdentity);
                    System.out.println(currentFileProgress.getName());
                }
                if (tableObject.getNetworkIdType().equals("AGG_ON")) {
                    currentSubset = findSubsetByUsingNetworkIdAndInitDate(generatedNetworkId, initDate);
                } else {
                    currentSubset = null;
                }
                if (currentSubset == null) {
                    currentSubset = new SamsungPmSubset_v2(headerOnList);
                    constantOperation(generatedNetworkId, initDate, generatedParentId, splittedLine[locationInfoOrder], splittedLine[gpOrder]);

                    for (RawCounterObject each : variableCounterListFromDb) {
                        //    System.out.print(each.getCounterNameFile() + " ");
                        int counterIndex = headerOnList.indexOf(each.getCounterNameFile());
                        currentSubset.putCounters(each, Double.parseDouble(splittedLine[counterIndex]));
                    }
                    //her counter group value eklemesi tamamlaninca counter 1 arttirlir.
                    currentSubset.incareseSize();
                    currentSubset.setInitDate(initDate);
                    currentSubset.setNetworkId(generatedNetworkId);
                    if (tableObject.getNetworkIdType().equals("AGG_ON")) {
                        subsetList.add(currentSubset);
                    } else {
                        //agregate olmayan direk isleme sokulur
                        writeIntoFiles(currentSubset);
                    }
                    currentSubset = null;
                } else {
                    for (RawCounterObject each : variableCounterListFromDb) {
                        //    System.out.print(each.getCounterNameFile() + " ");
                        int counterIndex = headerOnList.indexOf(each.getCounterNameFile());
                        currentSubset.aggValue(each, Double.parseDouble(splittedLine[counterIndex]));
                    }
                    currentSubset.incareseSize();
                }
                break;

        }

    }

    private void sam2gBtsLineOperation(String line) {
        String[] splittedLine = line.split("\\,");
        String initDate = splittedLine[initDateOrder];
        switch (tableObject.getTableType()) {
            case "Cell":
                String neName = splittedLine[2];
                SamsungCmObject object = ParserEngine_pm_csv_Samsung.findMyTopBscAndReturn(neName);
                if (object == null) {
                    //    System.out.println("Object not found");
                    return;
                }
                String generatedParentId = object.getTopParentId();
                String bscId = ParserEngine_pm_csv_Samsung.neNameToIdList.get(object.getNeName());
                String cellId = getCellId(splittedLine[locationInfoOrder]);
                String generatedNetworkId = generatedId(Integer.parseInt(bscId),
                        Integer.parseInt(cellId), vendorId, RanElementsInfo.CELL.getNeTypeId());

                if (tableObject.getNetworkIdType().equals("AGG_ON")) {
                    currentSubset = findSubsetByUsingNetworkIdAndInitDate(generatedNetworkId, initDate);
                } else {
                    currentSubset = null;
                }

                if (currentSubset == null) {
                    currentSubset = new SamsungPmSubset_v2(headerOnList);
                    constantOperation(generatedNetworkId, initDate, generatedParentId, splittedLine[locationInfoOrder], splittedLine[gpOrder]);

                    for (RawCounterObject each : variableCounterListFromDb) {
                        //    System.out.print(each.getCounterNameFile() + " ");
                        int counterIndex = headerOnList.indexOf(each.getCounterNameFile());
                        currentSubset.putCounters(each, Double.parseDouble(splittedLine[counterIndex]));
                    }
                    //her counter group value eklemesi tamamlaninca counter 1 arttirlir.
                    currentSubset.incareseSize();
                    currentSubset.setInitDate(initDate);
                    currentSubset.setNetworkId(generatedNetworkId);
                    if (tableObject.getNetworkIdType().equals("AGG_ON")) {
                        subsetList.add(currentSubset);
                    } else {
                        //agregate olmayan direk isleme sokulur
                        writeIntoFiles(currentSubset);
                    }
                    currentSubset = null;
                } else {
                    for (RawCounterObject each : variableCounterListFromDb) {
                        //    System.out.print(each.getCounterNameFile() + " ");
                        int counterIndex = headerOnList.indexOf(each.getCounterNameFile());
                        currentSubset.aggValue(each, Double.parseDouble(splittedLine[counterIndex]));
                    }
                    currentSubset.incareseSize();
                }
//                System.out.println(parentId);
//                System.out.println(generatedNetworkId);
                break;
        }
    }

    private void sam3gLineOperation(String line) {
        switch (tableObject.getNeType()) {
            case "SAM_RNC":
                sam3gRncLineOperation(line);
                break;
            case "SAM_NB":
                sam3gNbLineOperation(line);
                break;
        }
    }

    private void sam3gRncLineOperation(String line) {
        String[] splittedLine = line.split("\\,");
        String initDate = splittedLine[initDateOrder];
        switch (tableObject.getTableType()) {
            case "Cell":

                //top ne id db den dinamik alinacak file daki name den gelicek
                String neName = splittedLine[neNameOrder];
                String rawTopNeId = ParserEngine_pm_csv_Samsung.neNameToIdList.get(neName);
                if (rawTopNeId == null) {
                    return;
                }
                String rawCellId = getCellId(splittedLine[locationInfoOrder]);
                //String rawBaseStationId = getManagedElementBsId(splittedLine[locationInfoOrder]);
                String generatedParentId = generatedId(Integer.parseInt(rawTopNeId),
                        Integer.parseInt(rawTopNeId), vendorId, RanElementsInfo.BSCorRNC.getNeTypeId());
                String generatedNetworkId = generatedId(Integer.parseInt(rawTopNeId),
                        Integer.parseInt(rawCellId), vendorId, RanElementsInfo.CELL.getNeTypeId());

                if (tableObject.getNetworkIdType().equals("AGG_ON")) {
                    currentSubset = findSubsetByUsingNetworkIdAndInitDate(generatedNetworkId, initDate);
                } else {
                    currentSubset = null;
                }

                if (currentSubset == null) {
                    currentSubset = new SamsungPmSubset_v2(headerOnList);
                    constantOperation(generatedNetworkId, initDate, generatedParentId, splittedLine[locationInfoOrder], splittedLine[gpOrder]);

                    for (RawCounterObject each : variableCounterListFromDb) {
                        //    System.out.print(each.getCounterNameFile() + " ");
                        int counterIndex = headerOnList.indexOf(each.getCounterNameFile());
                        currentSubset.putCounters(each, Double.parseDouble(splittedLine[counterIndex]));
                    }
                    //her counter group value eklemesi tamamlaninca counter 1 arttirlir.
                    currentSubset.incareseSize();
                    currentSubset.setInitDate(initDate);
                    currentSubset.setNetworkId(generatedNetworkId);
                    if (tableObject.getNetworkIdType().equals("AGG_ON")) {
                        subsetList.add(currentSubset);
                    } else {
                        //agregate olmayan direk isleme sokulur
                        writeIntoFiles(currentSubset);
                    }
                    currentSubset = null;
                } else {
                    for (RawCounterObject each : variableCounterListFromDb) {
                        //    System.out.print(each.getCounterNameFile() + " ");
                        int counterIndex = headerOnList.indexOf(each.getCounterNameFile());
                        currentSubset.aggValue(each, Double.parseDouble(splittedLine[counterIndex]));
                    }
                    currentSubset.incareseSize();
                }
                break;

            case "SrcCell":

                //top ne id db den dinamik alinacak file daki name den gelicek
                neName = splittedLine[neNameOrder];
                rawTopNeId = ParserEngine_pm_csv_Samsung.neNameToIdList.get(neName);
                if (rawTopNeId == null) {
                    return;
                }
                rawCellId = getSrcCellId(splittedLine[locationInfoOrder]);
                //String rawBaseStationId = getManagedElementBsId(splittedLine[locationInfoOrder]);
                generatedParentId = generatedId(Integer.parseInt(rawTopNeId),
                        Integer.parseInt(rawTopNeId), vendorId, RanElementsInfo.BSCorRNC.getNeTypeId());
                generatedNetworkId = generatedId(Integer.parseInt(rawTopNeId),
                        Integer.parseInt(rawCellId), vendorId, RanElementsInfo.CELL.getNeTypeId());

                if (tableObject.getNetworkIdType().equals("AGG_ON")) {
                    currentSubset = findSubsetByUsingNetworkIdAndInitDate(generatedNetworkId, initDate);
                } else {
                    currentSubset = null;
                }

                if (currentSubset == null) {
                    currentSubset = new SamsungPmSubset_v2(headerOnList);
                    constantOperation(generatedNetworkId, initDate, generatedParentId, splittedLine[locationInfoOrder], splittedLine[gpOrder]);

                    for (RawCounterObject each : variableCounterListFromDb) {
                        //    System.out.print(each.getCounterNameFile() + " ");
                        int counterIndex = headerOnList.indexOf(each.getCounterNameFile());
                        currentSubset.putCounters(each, Double.parseDouble(splittedLine[counterIndex]));
                    }
                    //her counter group value eklemesi tamamlaninca counter 1 arttirlir.
                    currentSubset.incareseSize();
                    currentSubset.setInitDate(initDate);
                    currentSubset.setNetworkId(generatedNetworkId);
                    if (tableObject.getNetworkIdType().equals("AGG_ON")) {
                        subsetList.add(currentSubset);
                    } else {
                        //agregate olmayan direk isleme sokulur
                        writeIntoFiles(currentSubset);
                    }
                    currentSubset = null;
                } else {
                    for (RawCounterObject each : variableCounterListFromDb) {
                        //    System.out.print(each.getCounterNameFile() + " ");
                        int counterIndex = headerOnList.indexOf(each.getCounterNameFile());
                        currentSubset.aggValue(each, Double.parseDouble(splittedLine[counterIndex]));
                    }
                    currentSubset.incareseSize();
                }
                break;
        }
    }

    private void sam3gNbLineOperation(String line) {
        String[] splittedLine = line.split("\\,");
        String initDate = splittedLine[initDateOrder];
        switch (tableObject.getTableType()) {
            case "Cell":
                String neName = splittedLine[2];
                SamsungCmObject object = ParserEngine_pm_csv_Samsung.findMyTopRncAndReturn(neName);
                if (object == null) {
                                        return;
                }
                String generatedParentId = object.getTopParentId();
                String bscId = ParserEngine_pm_csv_Samsung.neNameToIdList.get(object.getNeName());
                String cellId = getCellId(splittedLine[locationInfoOrder]);
                String generatedNetworkId = generatedId(Integer.parseInt(bscId),
                        Integer.parseInt(cellId), vendorId, RanElementsInfo.CELL.getNeTypeId());

                if (tableObject.getNetworkIdType().equals("AGG_ON")) {
                    currentSubset = findSubsetByUsingNetworkIdAndInitDate(generatedNetworkId, initDate);
                } else {
                    currentSubset = null;
                }

                if (currentSubset == null) {
                    currentSubset = new SamsungPmSubset_v2(headerOnList);
                    constantOperation(generatedNetworkId, initDate, generatedParentId, splittedLine[locationInfoOrder], splittedLine[gpOrder]);

                    for (RawCounterObject each : variableCounterListFromDb) {
                        int counterIndex = headerOnList.indexOf(each.getCounterNameFile());
                        currentSubset.putCounters(each, Double.parseDouble(splittedLine[counterIndex]));
                    }
                    currentSubset.incareseSize();
                    currentSubset.setInitDate(initDate);
                    currentSubset.setNetworkId(generatedNetworkId);
                    if (tableObject.getNetworkIdType().equals("AGG_ON")) {
                        subsetList.add(currentSubset);
                    } else {
                        writeIntoFiles(currentSubset);
                    }
                    currentSubset = null;
                } else {
                    for (RawCounterObject each : variableCounterListFromDb) {
                        //    System.out.print(each.getCounterNameFile() + " ");
                        int counterIndex = headerOnList.indexOf(each.getCounterNameFile());
                        currentSubset.aggValue(each, Double.parseDouble(splittedLine[counterIndex]));
                    }
                    currentSubset.incareseSize();
                }
                break;
            case "Nodeb":
                neName = splittedLine[neNameOrder];
                object = ParserEngine_pm_csv_Samsung.neNameToObjectList3g.get(neName);
                if (object == null) {
                    return;
                }
                generatedParentId = object.getTopParentId();
                generatedNetworkId = object.getNeId();

                if (tableObject.getNetworkIdType().equals("AGG_ON")) {
                    currentSubset = findSubsetByUsingNetworkIdAndInitDate(generatedNetworkId, initDate);
                } else {
                    currentSubset = null;
                }

                if (currentSubset == null) {
                    currentSubset = new SamsungPmSubset_v2(headerOnList);
                    constantOperation(generatedNetworkId, initDate, generatedParentId, splittedLine[locationInfoOrder], splittedLine[gpOrder]);

                    for (RawCounterObject each : variableCounterListFromDb) {
                        //    System.out.print(each.getCounterNameFile() + " ");
                        int counterIndex = headerOnList.indexOf(each.getCounterNameFile());
                        currentSubset.putCounters(each, Double.parseDouble(splittedLine[counterIndex]));
                    }
                    //her counter group value eklemesi tamamlaninca counter 1 arttirlir.
                    currentSubset.incareseSize();
                    currentSubset.setInitDate(initDate);
                    currentSubset.setNetworkId(generatedNetworkId);
                    if (tableObject.getNetworkIdType().equals("AGG_ON")) {
                        subsetList.add(currentSubset);
                    } else {
                        //agregate olmayan direk isleme sokulur
                        writeIntoFiles(currentSubset);
                    }
                    currentSubset = null;
                } else {
                    for (RawCounterObject each : variableCounterListFromDb) {
                        int counterIndex = headerOnList.indexOf(each.getCounterNameFile());
                        currentSubset.aggValue(each, Double.parseDouble(splittedLine[counterIndex]));
                    }
                    currentSubset.incareseSize();
                }

                break;
        }
    }

    private SamsungPmSubset_v2 findSubsetByUsingNetworkIdAndInitDate(String networkId, String initDate) {
        for (SamsungPmSubset_v2 each : subsetList) {
            if (each.getInitDate().equals(initDate)
                    && each.getNetworkId().equals(networkId)) {
                return each;
            }
        }
        return null;
    }

    private String generatedId(Integer Parent_id, Integer ne_id, Integer vendorId, Integer elementType) {
        BigDecimal bd = new BigDecimal("10");
        BigDecimal resultBigDecimal = bd.pow(21).multiply(BigDecimal.valueOf(vendorId))
                .add((bd.pow(16).multiply(BigDecimal.valueOf(elementType))))
                .add((bd.pow(8).multiply(BigDecimal.valueOf(Parent_id))))
                .add(BigDecimal.valueOf(ne_id));
        return resultBigDecimal.toString();
    }

    private String getManagedElementBsId(String locationInfo) {
        String splittedLocation[] = locationInfo.split("\\/");
        for (String each : splittedLocation) {
            if (each.contains("ManagedElementBsId")) {
                return each.replace("ManagedElementBsId", "");
            }
        }
        return null;
    }

    private String getServingManagedElementBs(String locationInfo) {
        String splittedLocation[] = locationInfo.split("\\/");
        for (String each : splittedLocation) {
            if (each.contains("ServingManagedElementBs")) {
                return each.replace("ServingManagedElementBs", "");
            }
        }
        return null;
    }

    private String getServingCellIdentity(String locationInfo) {
        String splittedLocation[] = locationInfo.split("\\/");
        for (String each : splittedLocation) {
            if (each.contains("ServingCellIdentity")) {
                return each.replace("ServingCellIdentity", "");
            }
        }
        return null;
    }

    private String getTargetCellIdentity(String locationInfo) {
        String splittedLocation[] = locationInfo.split("\\/");
        for (String each : splittedLocation) {
            if (each.contains("TargetCellIdentity")) {
                return each.replace("TargetCellIdentity", "");
            }
        }
        return null;
    }

    private String getCellId(String locationInfo) {
        String splittedLocation[] = locationInfo.split("\\/");
        for (String each : splittedLocation) {

            if (each.contains("x") == false && each.contains("CellId")) {
                return each.replace("CellId", "");
            }
        }
        return null;
    }

    private String getSrcCellId(String locationInfo) {
        String splittedLocation[] = locationInfo.split("\\/");
        for (String each : splittedLocation) {
            if (each.contains("SrcCellId")) {
                return each.replace("SrcCellId", "");
            }
        }
        return null;
    }

    private void constantOperation(String generatedNetworkId, String initDate, String generatedParentId, String locationInfo, String gp) {
        String[] splittedLocation = locationInfo.split("\\/");
        for (RawCounterObject each : constantCounterListFromDb) {
            switch (each.getCounterNameDb()) {
                case "NETWORK_ID":
                    currentSubset.putProperty(each.getCounterNameDb(), generatedNetworkId);
                    break;
                case "DATA_DATE":
                    currentSubset.putProperty(each.getCounterNameDb(), initDate);
                    break;
                case "PARENT_ID":
                    currentSubset.putProperty(each.getCounterNameDb(), generatedParentId);
                    break;
                case "LOCATION_NAME":
                    currentSubset.putProperty(each.getCounterNameDb(), locationInfo);
                    break;
                case "GRAN_PERIOD":
                    currentSubset.putProperty(each.getCounterNameDb(), gp);
                    break;
                case "EX_TARGET_CELL_ID":
                    currentSubset.putProperty(each.getCounterNameDb(), "0");
                    break;
                case "EX_TARGET_PARENT_ID":
                    currentSubset.putProperty(each.getCounterNameDb(), "0");
                    break;
                case "EX_HANDOVER_CAUSE":
                    switch (tableObject.getTableName()) {
                        case "SAM2G_HO_NTR_RT_UTRN_IN_TO_GRN":
                            currentSubset.putProperty(each.getCounterNameDb(), splittedLocation[2]);
                            break;
                        default:
                            currentSubset.putProperty(each.getCounterNameDb(), splittedLocation[4]);
                            break;
                    }

                    break;
                case "EX_CHANNEL_TYPE":
                    currentSubset.putProperty(each.getCounterNameDb(), splittedLocation[2]);
                    break;
                case "EX_CAUSE_NAME":
                    currentSubset.putProperty(each.getCounterNameDb(), splittedLocation[3]);
                    break;
            }
        }
    }

    private void writeIntoFiles(SamsungPmSubset_v2 subset) {

        String fullHeader = subset.getHeader();
        String fullValue = subset.getValue();
        String myColumnHeader = tableObject.getFullColumnOrderUsingCounterNameFil("|");

        String result = CommonLibrary.get_RecordValue(fullHeader, fullValue, myColumnHeader, "0", "|", "|") + "\n";

        try {
            fileDate = dateFormatterfromFile.parse(subset.getInitDate());

            String localFilePath = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;

            SyncOutputController output = SyncOutputController.getInstance();

            output.writeIntoFiles(localFilePath, result);
        } catch (ParseException | ParserIOException ex) {

        }

    }

}
