/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.HW4GPmXmlSaxParser;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import static com.ttgint.parserEngine.common.AbsParserEngine.systemType;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.common.ParserSystems;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ParserEngineException;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import com.ttgint.parserEngine.systemProperties.RanElementsInfo;
import com.ttgint.parserEngine.systemProperties.RanVendorIdEnum;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author ibrahimegerci
 */
@ParserSystems(system = {
    @ParserSystem(operatorName = "KKTC-TELSIM", measType = "PM", systemType = "HW4G")
})
public class ParserEngine_pm_Huawei extends AbsParserEngine {

    public static final HashMap<String, Integer> neNameAndIdList = new HashMap<>();
    public static final HashMap<String, String> neNameAndIdList2G = new HashMap<>();
    public static final HashMap<String, String> neNameAndIdList3G = new HashMap<>();
    public static final HashMap<String, String> neNameAndIdList4G = new HashMap<>();
    public static final HashMap<String, String> neNameAndIdList5G = new HashMap<>();
    public static ConcurrentHashMap<String, Boolean> cellCounter = new ConcurrentHashMap<>();

    public ParserEngine_pm_Huawei(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() throws ParserEngineException {
        try {
            ResultSet rs = dbHelper.getAllActiveNE(operatorName, systemType);
            while (rs.next()) {
                neNameAndIdList.put(rs.getString("NE_NAME"), Integer.valueOf(rs.getString("RAW_NE_ID")));
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        switch (operatorName + " " + systemType) {
            case "VODAFONE HW2G":
                neNameAndIdList2G.putAll(CommonLibrary.getNetworkElementList(
                        "VODAFONE",
                        "HW2G",
                        "NORTHI_PARSER.OBJECTS_HW2G",
                        RanElementsInfo.BTSorNB.getNeTypeId()));

                neNameAndIdList2G.putAll(CommonLibrary.getNetworkElementList(
                        "VODAFONE",
                        "HW2G",
                        "NORTHI_PARSER.OBJECTS_HW2G",
                        RanElementsInfo.BSCorRNC.getNeTypeId()));
                break;
            case "VODAFONE HW3G":
                neNameAndIdList3G.putAll(CommonLibrary.getNetworkElementList(
                        "VODAFONE",
                        "HW3G",
                        "M2000.OBJECTS_HW3G",
                        RanElementsInfo.BTSorNB.getNeTypeId()));

                neNameAndIdList3G.putAll(CommonLibrary.getNetworkElementList(
                        "VODAFONE",
                        "HW3G",
                        "M2000.OBJECTS_HW3G",
                        RanElementsInfo.BSCorRNC.getNeTypeId()));
                break;
            case "VODAFONE HW4G":
                neNameAndIdList3G.putAll(CommonLibrary.getNetworkElementList(
                        "VODAFONE",
                        "HW3G",
                        "M2000.OBJECTS_HW3G",
                        RanElementsInfo.BTSorNB.getNeTypeId()));

                neNameAndIdList3G.putAll(CommonLibrary.getNetworkElementList(
                        "VODAFONE",
                        "HW3G",
                        "M2000.OBJECTS_HW3G",
                        RanElementsInfo.BSCorRNC.getNeTypeId()));

                neNameAndIdList4G.putAll(CommonLibrary.getNetworkElementList(
                        "VODAFONE",
                        "HW4G",
                        "NORTHI_PARSER.OBJECTS_HW4G",
                        RanElementsInfo.BTSorNB.getNeTypeId()));
                break;
            case "VODAFONE HW5G":
                neNameAndIdList5G.putAll(CommonLibrary.getNetworkElementList(
                        "VODAFONE",
                        "HW5G",
                        "NORTHI_PARSER.OBJECTS_HW5G",
                        RanElementsInfo.BTSorNB.getNeTypeId()));
                break;
            case "KKTC-TELSIM HW2G":
                neNameAndIdList2G.putAll(CommonLibrary.getNetworkElementList(
                        "KKTC-TELSIM",
                        "HW2G",
                        "NORTHI_PARSER_KKTC.OBJECTS_HW2G",
                        RanElementsInfo.BTSorNB.getNeTypeId()));

                neNameAndIdList2G.putAll(CommonLibrary.getNetworkElementList(
                        "KKTC-TELSIM",
                        "HW2G",
                        "NORTHI_PARSER_KKTC.OBJECTS_HW2G",
                        RanElementsInfo.BSCorRNC.getNeTypeId()));
                break;
            case "KKTC-TELSIM HW3G":
                neNameAndIdList3G.putAll(CommonLibrary.getNetworkElementList(
                        "KKTC-TELSIM",
                        "HW3G",
                        "NORTHI_PARSER_KKTC.OBJECTS_HW3G",
                        RanElementsInfo.BTSorNB.getNeTypeId()));

                neNameAndIdList3G.putAll(CommonLibrary.getNetworkElementList(
                        "KKTC-TELSIM",
                        "HW3G",
                        "NORTHI_PARSER_KKTC.OBJECTS_HW3G",
                        RanElementsInfo.BSCorRNC.getNeTypeId()));
                break;
            case "KKTC-TELSIM HW4G":
                neNameAndIdList3G.putAll(CommonLibrary.getNetworkElementList(
                        "KKTC-TELSIM",
                        "HW3G",
                        "NORTHI_PARSER_KKTC.OBJECTS_HW3G",
                        RanElementsInfo.BTSorNB.getNeTypeId()));

                neNameAndIdList3G.putAll(CommonLibrary.getNetworkElementList(
                        "KKTC-TELSIM",
                        "HW3G",
                        "NORTHI_PARSER_KKTC.OBJECTS_HW3G",
                        RanElementsInfo.BSCorRNC.getNeTypeId()));

                neNameAndIdList4G.putAll(CommonLibrary.getNetworkElementList(
                        "KKTC-TELSIM",
                        "HW4G",
                        "NORTHI_PARSER_KKTC.OBJECTS_HW4G",
                        RanElementsInfo.BTSorNB.getNeTypeId()));
                break;
            case "KKTC-TELSIM HW5G":
                neNameAndIdList5G.putAll(CommonLibrary.getNetworkElementList(
                        "KKTC-TELSIM",
                        "HW5G",
                        "NORTHI_PARSER_KKTC.OBJECTS_HW5G",
                        RanElementsInfo.BTSorNB.getNeTypeId()));
                break;
            default:
                return;
        }

    }

    @Override
    public void prepareParser() throws ParserEngineException {
        ExecutorService executorForXmlParser = Executors.newFixedThreadPool(numOfThreadParser);
        ArrayList<File> xmlFileList = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);
        System.out.println("Number of Xml files to Parse     : " + xmlFileList.size());
        System.out.println("Number of Active Subset          : " + TableWatcher.getInstance().activeSubsetSize());
        System.out.println("Number of Active Counter         : " + TableWatcher.getInstance().activeCounterSize());
        System.out.println("Number of active Network Element : " + neNameAndIdList.size());

        for (File xmlFile : xmlFileList) {
            if (xmlFile.getName().endsWith(".xml")) {
                try {
                    String neName = xmlFile.getName()
                            .split("\\_", 2)[1]
                            .replace(".xml.gz", "")
                            .replace(".xml", "");
                    if ((systemType.equals("HW2G") || systemType.equals("HW3G"))
                            && neName.split("\\_")[neName.split("\\_").length - 1].startsWith("P0")) {
                        neName = neName.replace("_" + neName.split("\\_")[neName.split("\\_").length - 1], "");
                    }
                    int neRawId = 0;
                    try {
                        neRawId = neNameAndIdList.get(neName);
                    } catch (Exception ex) {
                        System.out.println("Not found in ParserUsedNes: " + neName);
                        try {
                            xmlFile.delete();
                        } catch (Exception e) {
                            System.err.println("Not found delete error: " + xmlFile.getName());
                        }
                        continue;
                    }
                    String dataDate = xmlFile.getName().substring(1, 14).replace(".", "");

                    Runnable xmlParseThread = null;
                    switch (systemType) {
                        case "HW2G":
                            break;
                        case "HW3G":
                            break;
                        case "HW4G":
                            xmlParseThread = new HW4GPmXmlSaxParser(
                                    xmlFile,
                                    OperationSystemEnum.WINDOWS,
                                    ProgressTypeEnum.PRODUCT,
                                    neName,
                                    neRawId,
                                    dataDate,
                                    RanVendorIdEnum.VendorId4G.getVendorId()
                            );
                            break;
                        case "HW5G":
                            /*
                            xmlParseThread = new HW5GPmXmlSaxParser(xmlFile, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT,
                                    neName, neRawId, dataDate, RanVendorIdEnum.VendorId5G.getVendorId(), "NODEBID", neNameAndIdList5G.get(neName).split("\\|")[0]);
                             */
                            break;
                        case "HWCS":
                            break;
                    }
                    executorForXmlParser.execute(xmlParseThread);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        executorForXmlParser.shutdown();
        while (!executorForXmlParser.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
    }

    @Override
    public void loaderProcedures() throws ProcedureException {
        switch (operatorName + " " + systemType) {
            case "VODAFONE HW2G":
                dbHelper.callProceduresAfterParserWithThread("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(1)", "2G Loader");
                break;
            case "VODAFONE HW3G":
                boolean flag = false;
                for (String distinctDataDate : AbsParserEngine.loadedDatesUnieqeList.keySet()) {
                    if (distinctDataDate.endsWith("30")) {
                        flag = true;
                        break;
                    }
                }
                //Yarım saatlik data islemede calistirma laoder'i
                if (flag) {
                    dbHelper.callProceduresAfterParserWithThread("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(2)", "3G Loader");
                }
                break;
            case "VODAFONE HW4G":
                dbHelper.callProceduresAfterParserWithThread("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(21)", "4G Loader");
                break;
            case "VODAFONE HW5G":
                dbHelper.callProceduresAfterParserWithThread("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(32)", "5G Loader");
                break;
            case "VODAFONE HWCS":
                dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(35)", "HWCS Loader");
                break;
            case "KKTC-TELSIM HW2G":
                dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(13)", "KKTC 2G Loader");
                break;
            case "KKTC-TELSIM HW3G":
                dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(14)", "KKTC 3G Loader");
                break;
            case "KKTC-TELSIM HW4G":
                dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(34)", "KKTC 4G Loader");
                break;
            case "KKTC-TELSIM HW5G":
                break;
            case "KKTC-TELSIM HWCS":
                dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(15)", "HWCS NSS LOADER");
                dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(16)", "HWCS SGSN LOADER");
                dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(17)", "HWCS GGSN LOADER");
                break;
        }

        //PM Cell sayisi alinip loglaniyor
        HashMap<String, Integer> tmp = new HashMap<>();
        for (String data : cellCounter.keySet()) {
            String date = data.split("\\|")[0];
            Integer i = tmp.get(date);
            if (i == null) {
                tmp.put(date, 1);
            } else {
                tmp.put(date, ++i);
            }
        }
        for (String date : tmp.keySet()) {
            try {
                dbHelper.insertCellCount(date, tmp.get(date));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void objectProcedures() throws ProcedureException {
    }

}
