/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.MwPmCsvParser;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.MwPmCsvRegionParser;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.common.ParserSystems;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ParserEngineException;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author TurgutSimsek
 */
@ParserSystems(system = {
    @ParserSystem(systemType = "MW", measType = "PM", operatorName = "VODAFONE"),
    @ParserSystem(systemType = "MW", measType = "PM", operatorName = "KKTC-TELSIM")})
public class ParserEngine_pm_csv_MW extends AbsParserEngine {

    public static ConcurrentHashMap<String, String[]> subnetMap = new ConcurrentHashMap<>();

    public ParserEngine_pm_csv_MW(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() throws ParserEngineException {

    }

    @Override
    public void prepareParser() throws ParserEngineException {
        ArrayList<File> fileList = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);

        //Once region'lar aliniyor
        ExecutorService executor = Executors.newFixedThreadPool(AbsParserEngine.numOfThreadParser);
        for (File each : fileList) {
            if (each.getName().contains("NE_Report") && each.getName().endsWith(".csv")) {
                MwPmCsvRegionParser parserObject = new MwPmCsvRegionParser(each, OperationSystemEnum.UNIX, ProgressTypeEnum.PRODUCT);
                executor.execute(parserObject);
            }
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }

        System.out.println("SubnetLinkCount: " + subnetMap.size());
        executor = Executors.newFixedThreadPool(AbsParserEngine.numOfThreadParser);
        for (File each : fileList) {
            if (each.getName().endsWith(".csv")) {
                MwPmCsvParser parserObject = new MwPmCsvParser(each, OperationSystemEnum.UNIX, ProgressTypeEnum.PRODUCT);
                executor.execute(parserObject);
            }
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }

    }

    @Override
    public void loaderProcedures() throws ProcedureException {

        for (String distinctDataDate : AbsParserEngine.loadedDatesUnieqeList.keySet()) {
            dbHelper.callProceduresAfterParser("NORTHI_PARSER_SETTINGS.CLEAN_DUPLICATE_FOR_MW( TO_DATE('" + distinctDataDate.substring(0, 10).trim() + "' , 'MM/DD/YYYY'))", "MW clean duplicate");
        }

    }

    @Override
    public void objectProcedures() throws ProcedureException {

    }

}
