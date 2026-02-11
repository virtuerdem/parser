/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.DeviceWindowsParser;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.exceptions.ParserEngineException;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;

/**
 *
 * @author TURGUT SIMSEK
 */
@ParserSystem(operatorName = "VODAFONE", measType = "PM", systemType = "STORAGE-WINDOWS")
public class ParserEngine_Storage_Windows extends AbsParserEngine {

    public ParserEngine_Storage_Windows(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() throws ParserEngineException {

    }

    @Override
    public void prepareParser() throws ParserEngineException {
        ArrayList<File> listFiles = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);
        for (File each : listFiles) {
          //  if (each.getName().endsWith(".txt") || each.getName().endsWith(".csv")) {
            if (each.getName().endsWith(".csv")) {
                DeviceWindowsParser a1
                        = new DeviceWindowsParser(each, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
                a1.run();
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
