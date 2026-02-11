/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.VasCvsParser;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.ParserSystem;
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
 * @author abdullah.yakut
 */
@ParserSystem(systemType = "VAS", measType = "PM", operatorName = "VODAFONE")
public class ParserEngine_VAS extends AbsParserEngine {

    public ParserEngine_VAS(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() throws ParserEngineException {
       
    }

    @Override
    public void prepareParser() throws ParserEngineException {
        
       ArrayList<File> fileList = com.ttgint.parserEngine.commonLibrary.CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);

        ExecutorService executorForParser = Executors.newFixedThreadPool(AbsParserEngine.numOfThreadParser);
        for (File eachFile : fileList) {
            if (eachFile.getName().endsWith(".csv")) {
                executorForParser.execute(new VasCvsParser(eachFile, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT));
            }
        }
        executorForParser.shutdown();
        while (!executorForParser.isTerminated()) {
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
