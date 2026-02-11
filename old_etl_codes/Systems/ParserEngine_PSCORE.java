/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.PSCORETxtParser;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;

/**
 *
 * @author burakfircasiguzel
 */
@ParserSystem(systemType = "PSCORE", measType = "CM", operatorName = "VODAFONE")
public class ParserEngine_PSCORE extends AbsParserEngine {

    public static ArrayList<String> tableList = new ArrayList<>();

    public ParserEngine_PSCORE(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public final void setProperties() {

    }

    @Override
    public void prepareParser() {

        ArrayList<File> fileList = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);
        System.out.println("Number of File to Parse :" + fileList.size());

        ExecutorService executorForParser = Executors.newFixedThreadPool(AbsParserEngine.numOfThreadParser);
        for (File eachFile : fileList) {
            if (eachFile.getName().endsWith(".txt")) {
                executorForParser.execute(new PSCORETxtParser(eachFile, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT));
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
