package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.HW4GCmXmlSaxParser;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.HW5GCmXmlSaxParser;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import static com.ttgint.parserEngine.common.AbsParserEngine.numOfThreadParser;
import static com.ttgint.parserEngine.common.AbsParserEngine.operatorName;
import static com.ttgint.parserEngine.common.AbsParserEngine.systemType;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.common.ParserSystems;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import com.ttgint.parserEngine.systemProperties.RanElementsInfo;
import com.ttgint.parserEngine.systemProperties.RanVendorIdEnum;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author ibrahimegerci
 */
@ParserSystems(system = {
    @ParserSystem(operatorName = "VODAFONE", measType = "CM", systemType = "HW5GNew")
})
public class ParserEngine_cm_Huawei extends AbsParserEngine {

    public static int trxClassTypeId;
    public static int cellClassTypeId;
    public static int btsClassTypeId;
    public static int nodeBClassTypeId;
    public static int bscClassTypeId;
    public static int rncClassTypeId;
    public static int eNodeBClassTypeId;
    public static int gNodeBClassTypeId;

    public static int vendorId2G;
    public static int vendorId3G;
    public static int vendorId4G;
    public static int vendorId5G;

    public static HashMap<String, Integer> neNameAndIdList = new HashMap<>();

    public ParserEngine_cm_Huawei(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public final void setProperties() {
        trxClassTypeId = RanElementsInfo.TRX.getNeTypeId();
        cellClassTypeId = RanElementsInfo.CELL.getNeTypeId();
        btsClassTypeId = RanElementsInfo.BTSorNB.getNeTypeId();
        nodeBClassTypeId = RanElementsInfo.BTSorNB.getNeTypeId();
        bscClassTypeId = RanElementsInfo.BSCorRNC.getNeTypeId();
        rncClassTypeId = RanElementsInfo.BSCorRNC.getNeTypeId();
        eNodeBClassTypeId = RanElementsInfo.BTSorNB.getNeTypeId();
        gNodeBClassTypeId = RanElementsInfo.BTSorNB.getNeTypeId();

        vendorId2G = RanVendorIdEnum.VendorId2G.getVendorId();
        vendorId3G = RanVendorIdEnum.VendorId3G.getVendorId();
        vendorId4G = RanVendorIdEnum.VendorId4G.getVendorId();
        vendorId5G = RanVendorIdEnum.VendorId5G.getVendorId();

        try {
            ResultSet rs = dbHelper.getAllActiveNE(operatorName, systemType);
            while (rs.next()) {
                neNameAndIdList.put(rs.getString("NE_NAME"), Integer.parseInt(rs.getString("RAW_NE_ID")));
            }
            rs.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void prepareParser() {
        ExecutorService executorForXmlParser = Executors.newFixedThreadPool(numOfThreadParser);
        ArrayList<File> xmlFileList = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);
        System.out.println("Number of Xml files to Parse     : " + xmlFileList.size());
        for (File xmlFile : xmlFileList) {
            if (xmlFile.getName().endsWith(".xml")) {
                try {
                    String neName = xmlFile.getName()
                            .replace(xmlFile.getName().split("\\_")[0] + "_", "")
                            .replace("_" + xmlFile.getName().split("\\_")[xmlFile.getName().split("\\_").length - 1], "")
                            .replace("_" + xmlFile.getName().split("\\_")[xmlFile.getName().split("\\_").length - 2], "");
                    int neRawId = 0;
                    try {
                        neRawId = neNameAndIdList.get(neName);
                    } catch (Exception ex) {
                        System.out.println("Not found in ParserUsedNes: " + neName);
                        try {
                            xmlFile.delete();
                        } catch (Exception e) {
                            System.err.println("Not found delete error: " + neName);
                        }
                        continue;
                    }
                    String dataDate = xmlFile.getName().split("\\_")[xmlFile.getName().split("\\_").length - 1].substring(0, 8);

                    CommonLibrary.terbiye_Et(xmlFile.getPath());

                    Runnable xmlParseThread = null;
                    switch (systemType) {
                        case "HW2G":
                            break;
                        case "HW3G":
                            break;
                        case "HW4G":
                            xmlParseThread = new HW4GCmXmlSaxParser(xmlFile, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT,
                                    neName, neRawId, dataDate, vendorId4G);
                            break;
                        case "HW5G":
                            xmlParseThread = new HW5GCmXmlSaxParser(xmlFile, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT,
                                    neName, neRawId, dataDate, vendorId5G);
                            break;
                    }
                    executorForXmlParser.execute(xmlParseThread);
                } catch (IOException ex) {
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
    }

    @Override
    public void objectProcedures() throws ProcedureException {
        switch (operatorName + " " + systemType) {
            case "VODAFONE HW2G":
                dbHelper.callProceduresAfterParser("NORTHI_PARSER.OBJECTS_PROCESS_SR", "2G VFTR Object Procedure");
                dbHelper.callProceduresAfterParser("NORTHI_PARSER_SETTINGS.OBJECTS_HW2G_ELEMNT_UPDATE", "2g VFTR Element Update Procedure");
                break;
            case "VODAFONE HW3G":
                dbHelper.callProceduresAfterParser("M2000.OBJECTS_PROCESS", "3G VFTR Object Procedure");
                dbHelper.callProceduresAfterParser("NORTHI_PARSER_SETTINGS.OBJECTS_HW3G_ELEMNT_UPDATE", "3G VFTR Element Update Procedure");
                break;
            case "VODAFONE HW4G":
                dbHelper.callProceduresAfterParser("NORTHI_PARSER.OBJECTS_PROCESS_4G", "4G VFTR Object Procedure");
                dbHelper.callProceduresAfterParserWithThread("NORTHI_PARSER_SETTINGS.OBJECTS_HW4G_ELEMNT_UPDATE", "4G VFTR Element Update Procedure");
                break;
            case "VODAFONE HW5G":
                dbHelper.callProceduresAfterParser("NORTHI_PARSER.OBJECTS_PROCESS_5G", "5G VFTR Object Procedure");
                dbHelper.callProceduresAfterParserWithThread("NORTHI_PARSER_SETTINGS.OBJECTS_HW5G_ELEMNT_UPDATE", "5G VFTR Element Update Procedure");
                break;
            case "KKTC-TELSIM HW2G":
                dbHelper.callProceduresAfterParser("NORTHI_PARSER_KKTC.OBJECTS_PROCESS_2G", "2G KKTC Object Procedure");
                break;
            case "KKTC-TELSIM HW3G":
                dbHelper.callProceduresAfterParser("NORTHI_PARSER_KKTC.OBJECTS_PROCESS_3G", "3G KKTC Object Procedure");
                break;
            case "KKTC-TELSIM HW4G":
                break;
            case "KKTC-TELSIM HW5G":
                break;
            case "KKTC-TELSIM HWCS":
                break;
        }
    }
}
