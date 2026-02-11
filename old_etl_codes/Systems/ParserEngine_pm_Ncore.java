/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.NcoreXmlParser;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.DBSessionException;
import com.ttgint.parserEngine.exceptions.ParserEngineException;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author enesmalik.terzi, erdi.gurbuz
 */
@ParserSystem(systemType = "NCORE", measType = "PM", operatorName = "VODAFONE")
public class ParserEngine_pm_Ncore extends AbsParserEngine {

    public static ConcurrentHashMap<String, String> fileCounters = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> tableCounters = new ConcurrentHashMap<>();
    public static boolean flag = true;//Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == 7;

    public ParserEngine_pm_Ncore(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() throws ParserEngineException {
        if (flag) {
            try {
                ResultSet rss = AbsParserEngine.dbHelper.getCountersForAutoCreater(AbsParserEngine.operatorName, AbsParserEngine.systemType, AbsParserEngine.measType);
                while (rss.next()) {
                    String allCounters = rss.getString("ALL_COUNTERS");
                    String tableName = rss.getString("TABLE_NAME");
                    for (String neType : allCounters.split("\\|", 2)[0].split("\\,")) {
                        tableCounters.put(neType + "|" + allCounters.split("\\|", 2)[1], tableName);
                    }
                }
                rss.close();
            } catch (SQLException ex) {
                System.err.println(ex.getMessage());
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void prepareParser() throws ParserEngineException {
        ArrayList<File> fileList = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);
        ExecutorService executors = Executors.newFixedThreadPool(AbsParserEngine.numOfThreadParser);
        for (File each : fileList) {
            if (each.getName().endsWith(".xml")) {
                NcoreXmlParser hander = new NcoreXmlParser(each, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
                executors.execute(hander);
            }
        }
        executors.shutdown();
        while (!executors.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }

        if (flag) {
            try {
                insertTableAndCounterList();
            } catch (InterruptedException e) {
                System.err.println(e.toString());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void loaderProcedures() throws ProcedureException {
        dbHelper.callProceduresAfterParser("NORTHI_PARSER.UNITE_CSDB_APERTIOCOUNTERS", "CSDB_APERTIOCOUNTERS Proc");

        dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(11)", "NSNCSDB Loader");
        dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(25)", "NAP Loader");
        dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(4)", "NSS-CORE Loader");
        dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(31)", "Nokia PCRFDATA Loader");
    }

    @Override
    public void objectProcedures() throws ProcedureException {

    }

    private synchronized void insertTableAndCounterList() throws InterruptedException {
        System.out.println("*Counter insert started at " + CommonLibrary.get_CurrentDatetime("yyyy-MM-dd HH:mm:ss"));

        ConcurrentHashMap<String, String> tables = new ConcurrentHashMap<>();
        ConcurrentHashMap<String, String> counters = new ConcurrentHashMap<>();
        tableCounters.keySet().stream().parallel().forEach(e -> {
            tables.put(e.split("\\|")[0] + "|" + e.split("\\|")[1], tableCounters.get(e));
            counters.put(tableCounters.get(e) + "|" + e.split("\\|")[2], "");
            fileCounters.remove(e);
        });

        System.out.println("*Counter insert size: " + fileCounters.size() + " at " + CommonLibrary.get_CurrentDatetime("yyyy-MM-dd HH:mm:ss"));
        String counterDbName = "CN00T" + new SimpleDateFormat("yyMMddHHmm").format(new Date()) + "A";
        int[] number = {0};
        fileCounters.keySet().stream().forEach(e -> {
            number[0]++;
            try {
                String neType = e.split("\\|")[0];
                String functionSubsetName = e.split("\\|")[1];
                String counterName = e.split("\\|")[2];
                String tableName = (neType + "_" + functionSubsetName).replace(" ", "_").replace("-", "_").replace(",", "_").toUpperCase();

                if (!tables.containsKey(neType + "|" + functionSubsetName)) {
                    if (tableName.length() > 30) {
                        tableName = tableName.substring(0, 30);
                    }

                    if (tables.containsValue(tableName)) {
                        for (int i = 1; i < 100; i++) {
                            String tableNameTemp = tableName;
                            if (tableName.length() > 27) {
                                tableNameTemp = tableName.substring(0, 27);
                            }
                            tableNameTemp = tableNameTemp + String.format("%02d", i);
                            if (!tables.containsValue(tableNameTemp)) {
                                tableName = tableNameTemp;
                                break;
                            }
                        }
                    }

                    try {//newTable
                        addConstantColumns(neType, functionSubsetName, tableName);

                        dbHelper.insertParserRawTableListWithVendorIdAndSystemId("", tableName, "", "", -1, AbsParserEngine.systemType, neType, 15,
                                AbsParserEngine.measType, AbsParserEngine.operatorName, functionSubsetName, 3, 4);

                        tables.put(neType + "|" + functionSubsetName, tableName);
                    } catch (Exception ex) {
                        System.err.println(" newTable Error: for " + neType + " " + functionSubsetName + " " + ex.getMessage());
                    }
                } else {
                    tableName = tables.get(neType + "|" + functionSubsetName);
                }

                if (!counters.containsKey(tableName + "|" + counterName)) {
                    try {//newCounter
                        dbHelper.insertParserCounterList(counterDbName + String.format("%09d", number[0]), "NUMBER", "", counterName, "", "", tableName, -1,
                                AbsParserEngine.operatorName, AbsParserEngine.systemType, AbsParserEngine.measType, "VARIABLE", functionSubsetName, "");

                        counters.put(tableName + "|" + counterName, "");
                    } catch (DBSessionException ex) {
                        System.err.println(" newCounter Error: for " + neType + " " + functionSubsetName + " " + ex.getMessage());
                    }
                }

            } catch (Exception ex) {
                System.err.println(" forEach Error: for " + e + " " + ex.getMessage());
            }
        });

        System.out.println("*Counter insert finished at " + CommonLibrary.get_CurrentDatetime("yyyy-MM-dd HH:mm:ss"));
    }

    private void addConstantColumns(String neType, String functionSubsetName, String tableName) {
        try {
            dbHelper.insertParserCounterList("DATA_DATE", "DATE", "", "DATA_DATE", "", "YYYYMMDDHH24MI", tableName, -1,
                    AbsParserEngine.operatorName, AbsParserEngine.systemType, AbsParserEngine.measType, "CONSTANT", functionSubsetName, "");

            dbHelper.insertParserCounterList("NETWORK_ID", "NUMBER", "", "NETWORK_ID", "", "", tableName, -1,
                    AbsParserEngine.operatorName, AbsParserEngine.systemType, AbsParserEngine.measType, "CONSTANT", functionSubsetName, "");

            dbHelper.insertParserCounterList("OBJECT_NAME", "VARCHAR2", "255", "OBJECT_NAME", "", "", tableName, -1,
                    AbsParserEngine.operatorName, AbsParserEngine.systemType, AbsParserEngine.measType, "CONSTANT", functionSubsetName, "");

            dbHelper.insertParserCounterList("OSSNO", "VARCHAR2", "100", "OSSNO", "", "", tableName, -1,
                    AbsParserEngine.operatorName, AbsParserEngine.systemType, AbsParserEngine.measType, "CONSTANT", functionSubsetName, "");

            dbHelper.insertParserCounterList("DATA_BEGIN_TIME", "VARCHAR2", "20", "DATA_BEGIN_TIME", "", "", tableName, -1,
                    AbsParserEngine.operatorName, AbsParserEngine.systemType, AbsParserEngine.measType, "CONSTANT", functionSubsetName, "");

            dbHelper.insertParserCounterList("DATA_DURATION", "VARCHAR2", "20", "DATA_DURATION", "", "", tableName, -1,
                    AbsParserEngine.operatorName, AbsParserEngine.systemType, AbsParserEngine.measType, "CONSTANT", functionSubsetName, "");

            dbHelper.insertParserCounterList("DATA_END_TIME", "VARCHAR2", "20", "DATA_END_TIME", "", "", tableName, -1,
                    AbsParserEngine.operatorName, AbsParserEngine.systemType, AbsParserEngine.measType, "CONSTANT", functionSubsetName, "");
        } catch (Exception ex) {
            System.err.println(" addConstantColumns Error: for " + neType + " " + functionSubsetName + " " + ex.getMessage());
        }
    }
}
