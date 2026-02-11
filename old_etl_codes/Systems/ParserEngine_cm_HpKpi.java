/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.HpKpiCmTxtFileHandler;
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
@ParserSystem(systemType = "HP-KPI", measType = "CM", operatorName = "VODAFONE")
public class ParserEngine_cm_HpKpi extends AbsParserEngine {

    public ParserEngine_cm_HpKpi(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() throws ParserEngineException {
    }

    @Override
    public void prepareParser() throws ParserEngineException {
        ArrayList<File> fileList = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);
        ExecutorService executer = Executors.newFixedThreadPool(AbsParserEngine.numOfThreadParser);
        System.out.println("file size: " + fileList.size());
        for (File each : fileList) {
            if (!each.getName().endsWith(AbsParserEngine.integratedFileExtension)) {
                HpKpiCmTxtFileHandler handler
                        = new HpKpiCmTxtFileHandler(each, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
                executer.execute(handler);
            }
        }

        executer.shutdown();
        while (!executer.isTerminated()) {
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
