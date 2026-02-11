/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.ImsXmlPmHandler;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import static com.ttgint.parserEngine.common.AbsParserEngine.measType;
import static com.ttgint.parserEngine.common.AbsParserEngine.operatorName;
import static com.ttgint.parserEngine.common.AbsParserEngine.systemType;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.exceptions.ParserEngineException;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;

/**
 *
 * @author erdigurbuz
 */
@ParserSystem(operatorName = "VODAFONE", measType = "PM", systemType = "IMS")
public class ParserEngine_pm_IMS extends AbsParserEngine {

    private HashMap<String, String> allActiveFunctionSubsetIdTableName = new HashMap<>();

    public ParserEngine_pm_IMS(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() throws ParserEngineException {
    }

    @Override
    public void prepareParser() throws ParserEngineException {
        try {
            ResultSet rSet1 = dbHelper.getFunctionSubSetIDandName(systemType, measType, operatorName);
            while (rSet1.next()) {
                allActiveFunctionSubsetIdTableName.put(rSet1.getString("FUNCTIONSUBSET_ID"), rSet1.getString("TABLE_NAME"));
            }
            rSet1.close();
        } catch (Exception ex) {
        }

        ExecutorService executorForXmlParser = Executors.newFixedThreadPool(numOfThreadParser);

        for (File xmlFile : CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH)) {
            if (xmlFile.getName().endsWith(".xml")) {
                Runnable xmlParseThread = null;
                try {
                    xmlParseThread = new ImsXmlPmHandler(xmlFile, allActiveFunctionSubsetIdTableName, OperationSystemEnum.REDHAT, ProgressTypeEnum.PRODUCT);

                } catch (Exception ex) {
                    dbHelper.insertParserException(ex);
                    continue;
                }
                executorForXmlParser.execute(xmlParseThread);
            }
        }

        executorForXmlParser.shutdown();
        while (!executorForXmlParser.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
    }

    @Override
    public void loaderProcedures() throws ProcedureException {
        dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(20)", "IMS Loader");
    }

    @Override
    public void objectProcedures() throws ProcedureException {
    }

}
