/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.WTTXCsvParser;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ParserEngineException;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author ibrahimegerci
 */
@ParserSystem(systemType = "WTTX", measType = "PM", operatorName = "VODAFONE")
public class ParserEngine_pm_WTTX extends AbsParserEngine {

    public ParserEngine_pm_WTTX(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() throws ParserEngineException {
    }

    @Override
    public void prepareParser() throws ParserEngineException {

        ArrayList<File> fileList = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);
        ExecutorService executor = Executors.newFixedThreadPool(AbsParserEngine.numOfThreadParser);
        System.out.println("Csv file count for Parser: " + fileList.size());
        for (File each : fileList) {
            if (each.getName().endsWith(".csv")) {
                WTTXCsvParser parserObject = new WTTXCsvParser(each, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
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
    }

    @Override
    public void objectProcedures() throws ProcedureException {
        dbHelper.callProceduresAfterParser("NORTHI_LOADER.P_REFRESH_WTTX_LIST", "WTTX Object Refresh");
    }

}
