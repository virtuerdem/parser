/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.NecSpiderNetXmlParser;
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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author turgut.simsek
 */
@ParserSystem(operatorName = "VODAFONE", measType = "PM", systemType = "NEC-SPIDERNET")
public class ParserEngine_pm_NecSpiderNet extends AbsParserEngine {

    public static HashMap<String, String> neNameAndIdList = new HashMap<>();
    public static ConcurrentHashMap<String, HashMap<String, String>> objects = new ConcurrentHashMap<>();

    public ParserEngine_pm_NecSpiderNet(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() throws ParserEngineException {
    }

    @Override
    public void prepareParser() throws ParserEngineException {

        AbsParserEngine.vendorID = 8;
        try {
            ResultSet rSet = dbHelper.getNEIDandNameInfo(systemType);
            while (rSet.next()) {
                neNameAndIdList.put(rSet.getString("ne_name"), rSet.getString("raw_ne_id"));
            }
            rSet.close();
        } catch (Exception e) {
        }

        ExecutorService executorForXmlParser = Executors.newFixedThreadPool(numOfThreadParser);

        for (File xmlFile : CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH)) {
            if (xmlFile.getName().endsWith(".xml")) {
                Runnable xmlParseThread = null;
                try {
                    xmlParseThread = new NecSpiderNetXmlParser(xmlFile, OperationSystemEnum.REDHAT, ProgressTypeEnum.PRODUCT);
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

        //Object raw table
        if (!objects.isEmpty()) {

            try {
                File objFile = new File(AbsParserEngine.LOCALFILEPATH + "OBJECTS_NEC_SPIDERNET" + AbsParserEngine.integratedFileExtension);
                FileOutputStream objectFile;
                objectFile = new FileOutputStream(objFile);
                for (String date : objects.keySet()) {
                    HashMap<String, String> records = objects.get(date);
                    for (String value : records.values()) {
                        objectFile.write(value.getBytes());
                    }
                }
                objectFile.close();
            } catch (FileNotFoundException ex) {
            } catch (IOException ex) {
            }
        }
    }

    @Override
    public void loaderProcedures() throws ProcedureException {
        dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(8)", "NEC-SPIDERNET Loader");
    }

    @Override
    public void objectProcedures() throws ProcedureException {
        Date currentDate = new Date();
        int hour = Integer.parseInt(new SimpleDateFormat("HH").format(currentDate));
        if (hour % 4 == 0) {
            dbHelper.callProceduresAfterParser("NORTHI_PARSER.OBJECTS_PROCESS_NEC_SPIDERNET", "NEC-SPIDERNET Procedure");
            dbHelper.callProceduresAfterParser("NORTHI_PARSER_SETTINGS.OBJECTS_NEC_ELEMT_UPDATE", "NEC-SPIDERNET Element update Procedure");
        }
    }

}
