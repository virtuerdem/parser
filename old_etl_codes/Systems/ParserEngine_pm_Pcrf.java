/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.PcrfKpiCsvParser;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.PcrfPmXmlParser;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import static com.ttgint.parserEngine.common.AbsParserEngine.measType;
import static com.ttgint.parserEngine.common.AbsParserEngine.operatorName;
import static com.ttgint.parserEngine.common.AbsParserEngine.systemType;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.common.ParserSystems;
import com.ttgint.parserEngine.exceptions.ParserEngineException;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;

/**
 *
 * @author Turgut Simsek
 */
@ParserSystems(system = {
    @ParserSystem(systemType = "PCRF", measType = "PM", operatorName = "VODAFONE"),
    @ParserSystem(systemType = "PCRF-KPI", measType = "PM", operatorName = "VODAFONE")})
public class ParserEngine_pm_Pcrf extends AbsParserEngine {

    private final List<String> functionSubsetList;

    public ParserEngine_pm_Pcrf(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
        this.functionSubsetList = new ArrayList<>();
    }

    @Override
    public void setProperties() throws ParserEngineException {

    }

    @Override
    public void prepareParser() throws ParserEngineException {

        if (systemType.equals("PCRF")) {
            try (ResultSet rSet1 = dbHelper.getFunctionSubSetIDandName(systemType, measType, operatorName)) {
                while (rSet1.next()) {
                    functionSubsetList.add(rSet1.getString("FUNCTIONSUBSETNAME"));
                }
            } catch (Exception ex) {
            }
        }

        ArrayList<File> fileList = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);
        ExecutorService exeServer = Executors.newFixedThreadPool(AbsParserEngine.numOfThreadParser);
        for (File each : fileList) {
            if (each.getName().endsWith(AbsParserEngine.integratedFileExtension)) {
                continue;
            }
            if (systemType.equals("PCRF")) {
                PcrfPmXmlParser t1 = new PcrfPmXmlParser(each, functionSubsetList, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
                exeServer.execute(t1);
            } else if (systemType.equals("PCRF-KPI")) {
                PcrfKpiCsvParser t1 = new PcrfKpiCsvParser(each, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
                exeServer.execute(t1);
            }

        }
        exeServer.shutdown();
        while (!exeServer.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
    }

    @Override
    public void loaderProcedures() throws ProcedureException {
        dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(22)", "Pcrf Loader");

    }

    @Override
    public void objectProcedures() throws ProcedureException {
    }

}
