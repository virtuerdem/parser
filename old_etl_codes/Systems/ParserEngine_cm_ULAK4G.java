/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.Ulak4gXmlCmParser;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import static com.ttgint.parserEngine.common.AbsParserEngine.numOfThreadParser;
import static com.ttgint.parserEngine.common.AbsParserEngine.systemType;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ParserEngineException;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author ibrahimegerci
 */
@ParserSystem(operatorName = "VODAFONE", measType = "CM", systemType = "ULAK4G")
public class ParserEngine_cm_ULAK4G extends AbsParserEngine {

    public static ConcurrentHashMap<String, ArrayList<String>> neNameAndIdList = new ConcurrentHashMap<>();
    public static AtomicInteger counter = new AtomicInteger(1);
    public static int maxRawNeId;

    public ParserEngine_cm_ULAK4G(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() throws ParserEngineException {

    }

    @Override
    public void prepareParser() throws ParserEngineException {
        AbsParserEngine.vendorID = 7;
        ArrayList<String> row = null;
        maxRawNeId = AbsParserEngine.dbHelper.getMaxRawNeId(AbsParserEngine.systemType, AbsParserEngine.operatorName);
        try {
            ResultSet rSet2 = dbHelper.getNEIDandNameInfoAll(systemType);
            while (rSet2.next()) {
                row = new ArrayList<>();
                String neName = rSet2.getString("NE_NAME").split("\\_")[0] + "_" + rSet2.getString("NE_NAME").split("\\_")[1];
                row.add(rSet2.getString("RAW_NE_ID"));
                row.add(rSet2.getString("NE_NAME"));
                row.add(rSet2.getString("IS_ACTIVE"));
                neNameAndIdList.put(neName, row);
            }
            rSet2.close();
        } catch (Exception e) {
        }

        ExecutorService executorForXmlParser = Executors.newFixedThreadPool(numOfThreadParser);
        for (File xmlFile : CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH)) {
            if (xmlFile.getName().endsWith(".xml")) {
                Runnable xmlParseThread = null;
                try {
                    xmlParseThread = new Ulak4gXmlCmParser(xmlFile, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
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

    }

    @Override
    public void objectProcedures() throws ProcedureException {
        dbHelper.callProceduresAfterParser("NORTHI_PARSER.OBJECTS_PROCESS_ULAK4G", "Ulak4G Object");
    }

    public static int incrementCounter() {
        return (counter.getAndIncrement() + maxRawNeId);
    }
}
