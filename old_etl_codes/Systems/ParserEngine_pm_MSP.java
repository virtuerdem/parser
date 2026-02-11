/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.MspPmXmlHandler;
import com.ttgint.parserEngine.exceptions.ParserEngineException;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.numOfThreadParser;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
/**
 *
 * @author ibrahim.egerci
 */
@ParserSystem(systemType = "MSP", measType = "PM", operatorName = "VODAFONE")
public class ParserEngine_pm_MSP extends AbsParserEngine {

    public static HashMap<String, ArrayList<String>> kpiNameInfos = new HashMap<>();
    public static HashMap<String, ArrayList<String>> fileNameInfos = new HashMap<>();
    public static int maxSeq;

    public ParserEngine_pm_MSP(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() throws ParserEngineException {
        try {
            ResultSet rs1 = dbHelper.getKpiNameId(systemType, measType, operatorName);
            while (rs1.next()) {
                ArrayList<String> kpiNameInfo = new ArrayList<>();
                kpiNameInfo.add(0, rs1.getString("KPI_NAME_ID"));
                kpiNameInfo.add(1, rs1.getString("KPI_GROUP_ID"));
                kpiNameInfos.put(rs1.getString("NE_TYPE") + "-" + rs1.getString("KPI_NAME"), kpiNameInfo);
            }
            rs1.close();

            ResultSet rs2 = dbHelper.getKpiNameandLastWorkTime(systemType, measType, operatorName);
            while (rs2.next()) {
                ArrayList<String> fileNameInfo = new ArrayList<>();
                fileNameInfo.add(0, rs2.getString("PDP_PER_ROW"));
                fileNameInfo.add(1, rs2.getString("LAST_WORK_TIME"));
                fileNameInfos.put(rs2.getString("FILE_NAME"), fileNameInfo);
            }
            rs2.close();

            maxSeq = AbsParserEngine.dbHelper.getMaxKpiSeq();
        } catch (Exception ex) {
        }

    }

    @Override
    public void prepareParser() throws ParserEngineException {
        ExecutorService executorForXmlParser = Executors.newFixedThreadPool(numOfThreadParser);
        ArrayList<File> xmlFileList = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);

        for (File xmlFile : xmlFileList) {
            if (xmlFile.getName().endsWith(".xml")) {
                if (!fileNameInfos.containsKey(xmlFile.getName().replace(".xml", ""))) {
                    maxSeq++;
                    String kpiName = xmlFile.getName()
                            .replace(xmlFile.getName().split("\\-")[0] + "-", "")
                            .replace(xmlFile.getName().split("\\-")[1] + "-", "")
                            .replace(".xml", "");
                    try {
                        dbHelper.setNewNEandKpiName(AbsParserEngine.systemType, AbsParserEngine.measType, AbsParserEngine.operatorName,
                                xmlFile.getName().split("\\-")[0], xmlFile.getName().split("\\-")[1], kpiName, maxSeq);
                    } catch (Exception ex) {
                    }
                } else {
                    Runnable txtParseThread = new MspPmXmlHandler(xmlFile, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
                    executorForXmlParser.execute(txtParseThread);
                }

            }
        }

        executorForXmlParser.shutdown();
        while (!executorForXmlParser.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
        System.out.println("PM xmls parsed.");
    }

    @Override
    public void loaderProcedures() throws ProcedureException {
        dbHelper.callProceduresAfterParserWithThread("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(30)", "MSP Loader");
    }

    @Override
    public void objectProcedures() throws ProcedureException {
    }
}
