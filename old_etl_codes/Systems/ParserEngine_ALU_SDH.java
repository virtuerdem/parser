/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.AluSdhCsvFileHandler;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ParserEngineException;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author TurgutSimsek
 */
@ParserSystem(systemType = "ALUSDH", measType = "PM", operatorName = "VODAFONE")
public class ParserEngine_ALU_SDH extends AbsParserEngine {

    public ParserEngine_ALU_SDH(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);

    }

    public final static Set<String> dateList = Collections.synchronizedSet(new HashSet<String>());

    @Override
    public void setProperties() throws ParserEngineException {
    }

    @Override
    public void prepareParser() throws ParserEngineException {
        ArrayList<File> fileList = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);
        ExecutorService exeServer = Executors.newFixedThreadPool(AbsParserEngine.numOfThreadParser);
        for (File each : fileList) {
            if (each.getName().endsWith(AbsParserEngine.integratedFileExtension)) {
                continue;
            }
            AluSdhCsvFileHandler t1 = new AluSdhCsvFileHandler(each, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
            exeServer.execute(t1);
        }
        
        exeServer.shutdown();
        while (!exeServer.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
    }

    @Override
    public void loaderProcedures() throws ProcedureException {
        dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(12)", "Alusdh Loader");
    }

    @Override
    public void objectProcedures() throws ProcedureException {
    }

}
