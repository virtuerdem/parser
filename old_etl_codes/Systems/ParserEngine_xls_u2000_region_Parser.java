/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.U2000RegionXlsParser;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.U2000RegioncsvParserHandler;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.exceptions.ParserEngineException;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.parserHandler.FileHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;

/**
 *
 * @author TTGETERZI
 */
@ParserSystem(systemType = "HWU2000-REGION", measType = "PM", operatorName = "VODAFONE")
public class ParserEngine_xls_u2000_region_Parser extends AbsParserEngine {

    public static final HashMap<String, String> objectList = new HashMap<>();
    public static final String tableName = "U2000_REGION_INFO";
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");

    public ParserEngine_xls_u2000_region_Parser(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() throws ParserEngineException {

    }

    @Override
    public void prepareParser() throws ParserEngineException {
        ArrayList<File> fileList = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);
        for (File each : fileList) {
            FileHandler handler = null;
            if (each.getName().endsWith(".csv")) {
                handler = new U2000RegioncsvParserHandler(each, OperationSystemEnum.WINDOWS, ProgressTypeEnum.TEST);
            }
            if (each.getName().endsWith(".xls")) {
                handler = new U2000RegionXlsParser(each, OperationSystemEnum.WINDOWS, ProgressTypeEnum.TEST);
            }
            if (handler != null) {
                handler.run();
            }

        }
        System.out.println("Object size " + objectList.size());
        try {
            FileOutputStream output
                    = new FileOutputStream(new File(AbsParserEngine.LOCALFILEPATH + tableName + "-" + sdf.format(new Date()) + AbsParserEngine.integratedFileExtension));
            for (String each : ParserEngine_xls_u2000_region_Parser.objectList.keySet()) {
                String deviceName = each;
                String region = objectList.get(each);
                output.write(((deviceName + "|" + region).trim() + "\n").getBytes());
            }
            output.close();
        } catch (FileNotFoundException ex) {

        } catch (IOException ex) {

        }

    }
    
    @Override
    public void loaderProcedures() throws ProcedureException {

    }

    @Override
    public void objectProcedures() throws ProcedureException {

    }

}
