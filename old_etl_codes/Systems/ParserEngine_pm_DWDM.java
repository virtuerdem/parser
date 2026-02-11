package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.DWDMCsvParser;
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

@ParserSystem(systemType = "DWDM", measType = "PM", operatorName = "VODAFONE")
public class ParserEngine_pm_DWDM extends AbsParserEngine {

    public ParserEngine_pm_DWDM(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
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
                DWDMCsvParser parserObject = new DWDMCsvParser(each, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
                executor.execute(parserObject);
            } else {
                System.out.println("*File Deleted: " + each.getName());
                each.delete();
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
    }

}
