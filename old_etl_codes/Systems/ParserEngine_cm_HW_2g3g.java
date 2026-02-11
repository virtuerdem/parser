package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.HWCmSaxXmlParser2G;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.HWCmSaxXmlParser3G;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.HWCmSaxXmlParser4G;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.HWCmSaxXmlParser5G;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import static com.ttgint.parserEngine.common.AbsParserEngine.numOfThreadParser;
import static com.ttgint.parserEngine.common.AbsParserEngine.operatorName;
import static com.ttgint.parserEngine.common.AbsParserEngine.systemType;
import static com.ttgint.parserEngine.common.AbsParserEngine.vendorID;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.common.ParserSystems;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.logger.ParserEngineLogger;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import com.ttgint.parserEngine.systemProperties.RanElementsInfo;
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
 * @author TTGParserTeam©
 */
@ParserSystems(system = {
    @ParserSystem(systemType = "HW5G", measType = "CM", operatorName = "VODAFONE"),
    @ParserSystem(systemType = "HW4G", measType = "CM", operatorName = "VODAFONE"),
    @ParserSystem(systemType = "HW3G", measType = "CM", operatorName = "VODAFONE"),
    @ParserSystem(systemType = "HW2G", measType = "CM", operatorName = "VODAFONE"),
    @ParserSystem(systemType = "HW4G", measType = "CM", operatorName = "KKTC-TELSIM"),
    @ParserSystem(systemType = "HW3G", measType = "CM", operatorName = "KKTC-TELSIM"),
    @ParserSystem(systemType = "HW2G", measType = "CM", operatorName = "KKTC-TELSIM")
})
public class ParserEngine_cm_HW_2g3g extends AbsParserEngine {

    public static int CELLCLASSTYPEID;
    public static int BTSCLASSTYPEID;
    public static int BSCCLASSTYPEID;
    public static int TRXCLASSTYPEID;

    public static HashMap<String, Integer> neNameAndIdList = new HashMap<>();

    public ParserEngine_cm_HW_2g3g(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public final void setProperties() {
        AbsParserEngine.loggerOBJ.printLogForManager("Fixing Files", ParserEngineLogger.ManagerLogLevel.STATE);
        //  fixFiles();
        CELLCLASSTYPEID = RanElementsInfo.CELL.getNeTypeId();
        BTSCLASSTYPEID = RanElementsInfo.BTSorNB.getNeTypeId();
        if (systemType.equals("HW4G") || systemType.equals("HW5G")) {
            BSCCLASSTYPEID = RanElementsInfo.BTSorNB.getNeTypeId();
        } else {
            BSCCLASSTYPEID = RanElementsInfo.BSCorRNC.getNeTypeId();
        }
        TRXCLASSTYPEID = RanElementsInfo.TRX.getNeTypeId();
        String className = "CLASSNAMES-" + systemType;

        try {
            ResultSet rSet2 = dbHelper.getNEIDandNameInfo(systemType);
            while (rSet2.next()) {
                neNameAndIdList.put(rSet2.getString("ne_name"), Integer.parseInt(rSet2.getString("raw_ne_id")));
            }
            rSet2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void prepareParser() {
        switch (systemType) {
            case "HW3G":
                vendorID = 4;  // vendor ID bilgisi db den alinakca, northi_loader. northi_vendor_list
                break;
            case "HW2G":
                vendorID = 5;
                break;
            case "HW4G":
                vendorID = 6;
                break;
            case "HW5G":
                vendorID = 9;
                break;
        }

        ExecutorService executorForXmlParser = Executors.newFixedThreadPool(numOfThreadParser);
        ArrayList<File> xmlFileList = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);
        System.out.println("Xml file list " + xmlFileList.size());
        for (File xmlFile : xmlFileList) {

            if (xmlFile.getName().endsWith(".xml")) {
                try {
                    String neName = xmlFile.getName()
                            .replace(xmlFile.getName().split("\\_")[0] + "_", "")
                            .replace("_" + xmlFile.getName().split("\\_")[xmlFile.getName().split("\\_").length - 2], "")
                            .replace("_" + xmlFile.getName().split("\\_")[xmlFile.getName().split("\\_").length - 1], "");

                    int neRawID = 0;
                    try {
                        neRawID = neNameAndIdList.get(neName);
                    } catch (Exception ex) {
                        System.out.println("Not found in ParserUsedNes: " + neName);
                        continue;
                    }

                    String dataDate = xmlFile.getName().split("\\_")[xmlFile.getName().split("\\_").length - 1]
                            .replace(".xml", "");
                    dataDate = dataDate.substring(0, dataDate.length() - 2);
                    String xmlFilePath = xmlFile.getPath();

                    Runnable xmlParseThread = null;
                    switch (systemType) {
                        case "HW5G":
                            //CommonLibrary.terbiye_Et(xmlFile.getPath());
                            CommonLibrary.checkFile(xmlFilePath);
                            xmlParseThread = new HWCmSaxXmlParser5G(new File(xmlFilePath), OperationSystemEnum.WINDOWS,
                                    ProgressTypeEnum.PRODUCT, neName, neRawID, dataDate);
                            break;
                        case "HW4G":
                            //CommonLibrary.terbiye_Et(xmlFile.getPath());
                            CommonLibrary.checkFile(xmlFilePath);
                            xmlParseThread = new HWCmSaxXmlParser4G(new File(xmlFilePath), OperationSystemEnum.WINDOWS,
                                    ProgressTypeEnum.PRODUCT, neName, neRawID, dataDate);
                            break;
                        case "HW3G":
                            //CommonLibrary.terbiye_Et(xmlFile.getPath());
                            CommonLibrary.checkFile(xmlFilePath);
                            xmlParseThread = new HWCmSaxXmlParser3G(new File(xmlFilePath), OperationSystemEnum.WINDOWS,
                                    ProgressTypeEnum.PRODUCT, neName, neRawID, dataDate);
                            break;
                        case "HW2G":
                            //CommonLibrary.terbiye_Et(xmlFile.getPath());
                            CommonLibrary.checkFile(xmlFilePath);
                            xmlParseThread = new HWCmSaxXmlParser2G(new File(xmlFilePath), OperationSystemEnum.WINDOWS,
                                    ProgressTypeEnum.PRODUCT, neName, neRawID, dataDate);
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
        switch (operatorName) {
            case "VODAFONE":
                switch (systemType) {
                    case "HW5G":
                        dbHelper.callProceduresAfterParser("NORTHI_PARSER.OBJECTS_PROCESS_5G", "5G VFTR Object Procedure");
                        dbHelper.callProceduresAfterParserWithThread("NORTHI_PARSER_SETTINGS.OBJECTS_HW5G_ELEMNT_UPDATE", "5G VFTR Element Update Procedure");
                        break;
                    case "HW4G":
                        dbHelper.callProceduresAfterParser("NORTHI_PARSER.OBJECTS_PROCESS_4G", "4G VFTR Object Procedure");
                        dbHelper.callProceduresAfterParserWithThread("NORTHI_PARSER_SETTINGS.OBJECTS_HW4G_ELEMNT_UPDATE", "4G VFTR Element Update Procedure");
                        break;
                    case "HW3G":
                        dbHelper.callProceduresAfterParser("M2000.OBJECTS_PROCESS", "3G VFTR Object Procedure");
                        dbHelper.callProceduresAfterParser("NORTHI_PARSER_SETTINGS.OBJECTS_HW3G_ELEMNT_UPDATE", "3G VFTR Element Update Procedure");
                        break;
                    case "HW2G":
                        dbHelper.callProceduresAfterParser("NORTHI_PARSER.OBJECTS_PROCESS_SR", "2G VFTR Object Procedure");
                        dbHelper.callProceduresAfterParser("NORTHI_PARSER_SETTINGS.OBJECTS_HW2G_ELEMNT_UPDATE", "2g VFTR Element Update Procedure");
                        break;
                }
                break;
            case "KKTC-TELSIM":
                switch (systemType) {
                    case "HW4G":
                        dbHelper.callProceduresAfterParser("NORTHI_PARSER_KKTC.OBJECTS_PROCESS_4G", "4G KKTC Object Procedure");
                        break;
                    case "HW3G":
                        dbHelper.callProceduresAfterParser("NORTHI_PARSER_KKTC.OBJECTS_PROCESS_3G", "3G KKTC Object Procedure");
                        break;
                    case "HW2G":
                        dbHelper.callProceduresAfterParser("NORTHI_PARSER_KKTC.OBJECTS_PROCESS_2G", "2G KKTC Object Procedure");
                        break;
                }
                break;
        }
    }
}
