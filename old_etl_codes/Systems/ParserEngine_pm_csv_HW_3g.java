/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import java.io.File;
import com.ttgint.parserEngine.common.AbsParserEngine;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.HW3GPmCsvParser3G;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.common.ParserSystems;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import com.ttgint.parserEngine.systemProperties.RanElementsInfo;

/**
 *
 * @author TTGETERZI
 */
@ParserSystems(system = {
    @ParserSystem(systemType = "HW3Gcsv", measType = "PM", operatorName = "KKTC-TELSIM")
})
public class ParserEngine_pm_csv_HW_3g extends AbsParserEngine {

    public static HashMap<String, String> functionSubSetIdList = new HashMap<>();
    public static HashMap<String, String> nodebNameAndIDList = new HashMap<>();
    public static HashMap<String, String> rncNameAndIDList = new HashMap<>();
    public static int CELLCLASSTYPEID = RanElementsInfo.CELL.getNeTypeId();

    public ParserEngine_pm_csv_HW_3g(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() {
    }

    @Override
    public void prepareParser() {
        switch (systemType) {
            case "HW3Gcsv":
                vendorID = 4;
                break;
        }
        try {
            ResultSet rSet1 = dbHelper.getFunctionSubSetIDandName(systemType, measType, operatorName);
            while (rSet1.next()) {
                functionSubSetIdList.put(rSet1.getString("functionsubset_id"), rSet1.getString("table_name"));
            }
            rSet1.close();

            ResultSet rSet2 = dbHelper.getNodebNameAndIDList(AbsParserEngine.rawSchemaName);
            while (rSet2.next()) {
                nodebNameAndIDList.put(rSet2.getString("ne_name"), rSet2.getString("ne_id"));
            }
            rSet2.close();

            ResultSet rSet3 = dbHelper.getRncNameAndIDList(AbsParserEngine.rawSchemaName);
            while (rSet3.next()) {
                rncNameAndIDList.put(rSet3.getString("ne_id"), rSet3.getString("raw_ne_id"));
            }
            rSet3.close();
        } catch (Exception e) {
        }
        ExecutorService executorCsvParser = Executors.newFixedThreadPool(numOfThreadParser);
        ArrayList<File> csvFiles = CommonLibrary.list_AllFilesAsFile(LOCALFILEPATH);
        for (File csvFile : csvFiles) {

            if (csvFile.getName().endsWith(".csv")) {
                Runnable pmCsvParser3g = null;

                pmCsvParser3g = new HW3GPmCsvParser3G(csvFile, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);

                executorCsvParser.execute(pmCsvParser3g);
            }
        }
        executorCsvParser.shutdown();
        while (!executorCsvParser.isTerminated()) {
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
    }

}
