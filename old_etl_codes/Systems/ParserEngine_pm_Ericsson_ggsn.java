/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import java.io.File;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.EricssonEGGSNParserHandler;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.exceptions.ParserEngineException;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;

/**
 *
 * @author TTGETERZI
 */
@ParserSystem(systemType = "EGGSN", measType = "PM", operatorName = "VODAFONE")
public class ParserEngine_pm_Ericsson_ggsn extends AbsParserEngine {

    public ParserEngine_pm_Ericsson_ggsn(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() throws ParserEngineException {
    }

    @Override
    public void prepareParser() throws ParserEngineException {
        System.out.println("Getting Information From DB");

        File[] fileList = new File(AbsParserEngine.LOCALFILEPATH).listFiles();
        ExecutorService executorForFileParse
                = Executors.newFixedThreadPool(AbsParserEngine.numOfThreadParser);
        for (File eachFile : fileList) {
            if (eachFile.getName().endsWith("_10ADELTA.xml")) {
                executorForFileParse.execute(new EricssonEGGSNParserHandler(eachFile,
                        OperationSystemEnum.WINDOWS,
                        ProgressTypeEnum.PRODUCT));
            }
        }

        executorForFileParse.shutdown();
        while (!executorForFileParse.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
    }

    @Override
    public void loaderProcedures() throws ProcedureException {
        dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(7)", "EGGSN Loader");
    }

    @Override
    public void objectProcedures() throws ProcedureException {
    }

}
