/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.SamsungCmXmlMainParserNew;
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
@ParserSystem(systemType = "SAMSUNG", operatorName = "VODAFONE", measType = "CM")
public class ParserEngine_cm_xml_Samsung extends AbsParserEngine {

    public final static String TABLE_NAME_3G = "OBJECTS_SAM3G_RAW";
    public final static String TABLE_NAME_2G = "OBJECTS_SAM2G_RAW";
    public final static String RNCCellMapTABLE_NAME = "SAM_RNC_CELL_RELATION";

    public final static ConcurrentHashMap<String, String> neNameToIdList = new ConcurrentHashMap<>();

    public final static List<String> copyNeList
            = Collections.synchronizedList(new ArrayList<String>());

    public ParserEngine_cm_xml_Samsung(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() throws ParserEngineException {

    }

    @Override
    public void prepareParser() throws ParserEngineException {
        ResultSet rss = AbsParserEngine.dbHelper.getActiveSamsungBscAndRnc();

        try {
            while (rss.next()) {
                String neName = rss.getString("NE_NAME");
                String neId = rss.getString("RAW_NE_ID");
                neNameToIdList.put(neName, neId);
                copyNeList.add(neName);
            }
        } catch (SQLException ex) {
//            Logger.getLogger(ParserEngine_cm_xml_Samsung.class.getName()).log(Level.SEVERE, null, ex);
        }

        ArrayList<File> fileList = CommonLibrary.list_AllFilesAsFile(LOCALFILEPATH);
        for (File each : fileList) {
            if (each.getName().endsWith(AbsParserEngine.integratedFileExtension)) {
                each.delete();
            }
        }
        fileList = CommonLibrary.list_AllFilesAsFile(LOCALFILEPATH);
        ExecutorService executor = Executors.newFixedThreadPool(AbsParserEngine.numOfThreadParser);
        for (File each : fileList) {

            if (each.getName().contains("ManagementNode")
                    && each.getName().endsWith(".xml")) {
                try {
                    CommonLibrary.terbiye_Et2(each.getPath());
                } catch (IOException ex) {
                }
                SamsungCmXmlMainParserNew parserObject
                        = new SamsungCmXmlMainParserNew(each, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
                executor.execute(parserObject);
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

        dbHelper.callProceduresAfterParser("NORTHI_PARSER.OBJECTS_PROCESS_SAM2G", "2G Samsung Object Procedure");

        dbHelper.callProceduresAfterParser("NORTHI_PARSER.OBJECTS_PROCESS_SAM3G", "3G Samsung Object Procedure");

        dbHelper.callProceduresAfterParser("NORTHI_PARSER_SETTINGS.OBJECTS_SAM2G_ELEMNT_UPDATE", "2G Samsung Element update Procedure");

        dbHelper.callProceduresAfterParser("NORTHI_PARSER_SETTINGS.OBJECTS_SAM3G_ELEMNT_UPDATE", "3G Samsung Element update Procedure");

    }

}
