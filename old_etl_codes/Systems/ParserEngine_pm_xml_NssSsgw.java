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
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.NssSsgwXmlParserHandler;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.exceptions.ParserEngineException;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;

/**
 *
 * @author erdigurbuz
 */
@ParserSystem(systemType = "NSS-SSGW", measType = "PM", operatorName = "VODAFONE")
public class ParserEngine_pm_xml_NssSsgw extends AbsParserEngine {

    public static final HashMap<String, String> neNametoIdList = new HashMap<>();

    public ParserEngine_pm_xml_NssSsgw(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() throws ParserEngineException {

    }

    @Override
    public void prepareParser() throws ParserEngineException {
        ResultSet rss = AbsParserEngine.dbHelper.getActiveParserUsedNes(systemType);
        try {
            while (rss.next()) {
                String neName = rss.getString("NE_NAME");
                String neId = rss.getString("RAW_NE_ID");
                neNametoIdList.put(neName, neId);
            }
        } catch (Exception e) {

        }

        ArrayList<File> fileList = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);
        ExecutorService serv = Executors.newFixedThreadPool(10);
        for (File each : fileList) {
            try {
                NssSsgwXmlParserHandler parserObject
                        = new NssSsgwXmlParserHandler(each, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
                serv.execute(parserObject);

            } catch (Exception ex) {

            }
        }
        serv.shutdown();
        while (!serv.isTerminated()) {
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
