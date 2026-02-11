/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.DefneCliPmTxtFileHandler;
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
@ParserSystem(systemType = "DEFNE_CLI", measType = "PM", operatorName = "VODAFONE")
public class ParserEngine_DefneCliPm extends AbsParserEngine {

    public ParserEngine_DefneCliPm(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() throws ParserEngineException {
    }

    @Override
    public void prepareParser() throws ParserEngineException {
        ArrayList<File> fileList = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);
        ExecutorService executerServer = Executors.newFixedThreadPool(AbsParserEngine.numOfThreadParser);
        for (File each : fileList) {
            if (each.getName().endsWith(".csv")) {
                DefneCliPmTxtFileHandler t1 = new DefneCliPmTxtFileHandler(each, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
                executerServer.execute(t1);
            }
        }

        executerServer.shutdown();
        while (!executerServer.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
    }

    @Override
    public void loaderProcedures() throws ProcedureException {
        dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(33)", "DEFNE_CLI Loader");
    }

    @Override
    public void objectProcedures() throws ProcedureException {
    }

}
