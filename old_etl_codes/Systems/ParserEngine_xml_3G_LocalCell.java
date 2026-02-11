/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.Hw3NodeBXmlParser;
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
@ParserSystem(systemType = "HW3G-LOCALCELL", measType = "PM", operatorName = "VODAFONE")
public class ParserEngine_xml_3G_LocalCell extends AbsParserEngine {

    public static final ConcurrentHashMap<String, String> rncNameTogeneratedIdMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, String> nodebNameToNodeBId = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, String> neNameToNerawId = new ConcurrentHashMap<>();

    public ParserEngine_xml_3G_LocalCell(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() throws ParserEngineException {

        try {
            ResultSet rss = AbsParserEngine.dbHelper.getActiveRncNameAndIdByLastDate(AbsParserEngine.operatorName);

            while (rss.next()) {
                String neName = rss.getString("NE_NAME");
                String neId = rss.getString("NE_ID");
                String dataDate = rss.getString("DATA_DATE");
                rncNameTogeneratedIdMap.put(neName, neId);
                ResultSet rss2 = AbsParserEngine.dbHelper.getActiveNodeBNameAndId(AbsParserEngine.operatorName, dataDate, neId);
                while (rss2.next()) {
                    String nodebName = rss2.getString("NE_NAME");
                    String nodebId = rss2.getString("NE_ID");
                    if (nodebNameToNodeBId.containsKey(nodebName) == false) {
                        nodebNameToNodeBId.put(nodebName, nodebId);
                    }
                }
                rss2.close();

            }
            rss.close();
        } catch (SQLException ex) {

        }
        try {
            ResultSet rss = dbHelper.getNEIDandNameInfo("HW3G");
            while (rss.next()) {
                String neName = rss.getString("NE_NAME");
                String rawNeId = rss.getString("RAW_NE_ID");
//                System.out.println(neName + " " + rawNeId);
                neNameToNerawId.put(neName, rawNeId);
            }
        } catch (Exception ex) {
            Logger.getLogger(ParserEngine_xml_3G_LocalCell.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Active Rnc   Size : " + rncNameTogeneratedIdMap.size());
        System.out.println("Active NodeB Size : " + nodebNameToNodeBId.size());
    }

    @Override
    public void prepareParser() throws ParserEngineException {
        ArrayList<File> localFileList = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);
        ExecutorService parserExecutor = Executors.newFixedThreadPool(AbsParserEngine.numOfThreadParser);
        for (File each : localFileList) {
            if (each.getName().endsWith(".xml")) {
                Hw3NodeBXmlParser parserObject
                        = new Hw3NodeBXmlParser(each, OperationSystemEnum.REDHAT, ProgressTypeEnum.PRODUCT);
                parserExecutor.execute(parserObject);
            }
        }
        parserExecutor.shutdown();
        while (!parserExecutor.isTerminated()) {
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
