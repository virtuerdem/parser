/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.WdmCfgTxtParser;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.WdmTxtParser;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.WdmCsvParser;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.WdmCsvReportParser;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.WdmObjectListener;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ParserEngineException;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author turgut.simsek
 */
@ParserSystem(systemType = "WDM", measType = "PM", operatorName = "VODAFONE")
public class ParserEngine_pm_WDM extends AbsParserEngine {

    private static final String OBJECT_TABLE_NAME = "WDM_OBJECTS_TEMP";
    public static HashMap<String, String> resourceNameToObjectId = new HashMap<>();
    public static HashMap<String, String> mediaCapacityInterfaceWrongStringForMail = new HashMap<>();

    public ParserEngine_pm_WDM(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() throws ParserEngineException {

        try {

            try {
                ResultSet rss = AbsParserEngine.dbHelper.getWdmObjects(AbsParserEngine.rawSchemaName);
                while (rss.next()) {
                    String objectId = rss.getString(("OBJECT_ID"));
                    String resourceName = rss.getString("RESOURCE_NAME");
                    resourceNameToObjectId.put(resourceName, objectId);
                }
            } catch (Exception ex) {

            }
            System.out.println("Active Object Size :" + resourceNameToObjectId.size());
            Thread.sleep(2000);
        } catch (InterruptedException ex) {

        }
        System.out.println("Parser Started");

    }

    @Override
    public void prepareParser() throws ParserEngineException {

        ArrayList<File> fileList = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);
        ExecutorService executor = Executors.newFixedThreadPool(AbsParserEngine.numOfThreadParser);
        for (File each : fileList) {
            if (each.getName().endsWith(".csv") && each.getName().contains("NE_Report")) {
                WdmCsvReportParser parserObject = new WdmCsvReportParser(each, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
                executor.execute(parserObject);
            } else if (each.getName().endsWith(".csv") && each.getName().startsWith("PM_IG")) {
                WdmCsvParser parserObject = new WdmCsvParser(each, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
                executor.execute(parserObject);
            } else if (each.getName().endsWith(".txt") && each.getName().contains("Cfg")) {
                WdmCfgTxtParser cfgTxtParser = new WdmCfgTxtParser(each, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
                executor.execute(cfgTxtParser);
            } else if (each.getName().endsWith(".txt") && (each.getName().contains("_pfm_WDM_") || each.getName().contains("_pfm_SDH_"))) {
                WdmTxtParser txtParser = new WdmTxtParser(each, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
                executor.execute(txtParser);
            }
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }

        if (WdmObjectListener.objectList.isEmpty() == false) {
            File objectFile = new File((AbsParserEngine.LOCALFILEPATH + OBJECT_TABLE_NAME + integratedFileExtension));
            FileOutputStream out;
            try {
                out = new FileOutputStream(objectFile);
                for (WdmObjectListener.WdmObject each : WdmObjectListener.objectList.values()) {
                    String line = each.toString();
                    out.write((line + "\n").getBytes());
                }
                out.close();
            } catch (FileNotFoundException ex) {
            } catch (IOException ex) {
            }
        }

        for (File file : fileList) {
            file.deleteOnExit();
        }

    }

    @Override
    public void loaderProcedures() throws ProcedureException {
    }

    @Override
    public void objectProcedures() throws ProcedureException {

        dbHelper.callProceduresAfterParser("NORTHI_PARSER.WDM_OBJECTS_PROCS", "HW WDM Update Objects");

    }

}
