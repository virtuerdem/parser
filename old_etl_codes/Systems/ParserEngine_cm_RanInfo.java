/*
 * To change this license header, choose License Headers in Project Properties.j
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.RanInfoCsvParser;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.RanInfoNewCsvParser;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.RanInfoXlsParser;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import static com.ttgint.parserEngine.common.AbsParserEngine.measType;
import static com.ttgint.parserEngine.common.AbsParserEngine.numOfThreadParser;
import static com.ttgint.parserEngine.common.AbsParserEngine.operatorName;
import static com.ttgint.parserEngine.common.AbsParserEngine.systemType;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author erdigurbuz
 */
@ParserSystem(systemType = "RANINFO", measType = "CM", operatorName = "VODAFONE")
public class ParserEngine_cm_RanInfo extends AbsParserEngine {

    private final String currentDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
    public final static String raninfoControlDateFromFileHousekeep = "20220707";
    public HashMap<String, String> mailQueue = new HashMap<>();

    public ParserEngine_cm_RanInfo(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() {
        /*
        *Eksik file tespit
         */
        List<String> missingFiles = new ArrayList<>();
        HashMap<String, Integer> fileMap = null;
        HashMap<Integer, String> serverIpMap = null;
        HashMap<String, String> mailList = new HashMap<>();
        mailList.put("/ftproot/2G_TRX/",
                "barlas.basaran@vodafone.com, "
                + "tolga.nural@vodafone.com, fatma.haydarlar@vodafone.com, "
                + "hakan.coskun1@vodafone.com, yigit.yetiz1@vodafone.com, "
                + "ran_planning.tr@vodafone.com, tan.cetin@vodafone.com, "
                + "northi.system@ttgint.com, cemre.mengu@ttgint.com");
        mailList.put("/opt/oss/server/var/fileint/cm/Report/",
                "gokhan.ozpinar@vodafone.com, "
                + "tolga.nural@vodafone.com, fatma.haydarlar@vodafone.com, "
                + "hakan.coskun1@vodafone.com, yigit.yetiz1@vodafone.com, "
                + "ran_planning.tr@vodafone.com, tan.cetin@vodafone.com, "
                + "northi.system@ttgint.com, cemre.mengu@ttgint.com");
        mailList.put("/ftproot/btk/",
                "gokhan.ozpinar@vodafone.com, "
                + "tolga.nural@vodafone.com, fatma.haydarlar@vodafone.com, "
                + "hakan.coskun1@vodafone.com, yigit.yetiz1@vodafone.com, "
                + "ran_planning.tr@vodafone.com, tan.cetin@vodafone.com, "
                + "northi.system@ttgint.com, cemre.mengu@ttgint.com");
        mailList.put("/ftproot/TTGReport/",
                "gokhan.ozpinar@vodafone.com, barlas.basaran@vodafone.com, "
                + "tolga.nural@vodafone.com, fatma.haydarlar@vodafone.com, "
                + "hakan.coskun1@vodafone.com, yigit.yetiz1@vodafone.com, "
                + "ran_planning.tr@vodafone.com, tan.cetin@vodafone.com, "
                + "northi.system@ttgint.com, cemre.mengu@ttgint.com");
        try {
            fileMap = dbHelper.getRanInfoMissingFiles(raninfoControlDateFromFileHousekeep);
        } catch (Exception ex) {
            dbHelper.insertParserException(ex);
        }
        for (String fileName : fileMap.keySet()) {
            missingFiles.add(fileName);
            System.out.println("Missing File: " + fileName);
        }

        try {
            serverIpMap = dbHelper.getPathFromConnectionId(operatorName, systemType, measType);
        } catch (Exception ex) {
            dbHelper.insertParserException(ex);
        }

        if (!missingFiles.isEmpty()) {
            for (String missingFileName : missingFiles) {
                String m2000Addr = missingFileName.split("\\+")[0];
                String path = "";
                try {
                    path = serverIpMap.get(fileMap.get(missingFileName));
                } catch (Exception ex) {
                    dbHelper.insertParserException(ex);
                }
                String mailLsStr = mailList.get(path);
                missingFileName = missingFileName.split("\\+")[1];
                if (missingFileName.contains("cellinfoson") || missingFileName.contains("2G_TRX")) {
                    missingFileName = missingFileName + ".xls";
                } else if (missingFileName.contains("ConfigurationReport_result")) {
                    missingFileName = missingFileName + "_" + currentDate + ".zip";
                } else {
                    missingFileName = missingFileName + "_" + currentDate + ".csv";
                }
                if (mailQueue.get(mailLsStr) != null) {
                    mailQueue.put(mailLsStr, m2000Addr + "+" + path + missingFileName + "\n" + mailQueue.get(mailLsStr));
                } else {
                    mailQueue.put(mailLsStr, m2000Addr + "+" + path + missingFileName + "\n");
                }
            }
        }

        for (String missingFileList : mailQueue.values()) {
            System.out.println("Missing Files: \n" + missingFileList);
        }
    }

    @Override
    public void prepareParser() {
        List<File> fileList = CommonLibrary.list_AllFilesAsFile(LOCALFILEPATH);
        System.out.println("Parsed file count: " + fileList.size());

        List<File> fileParsList = new ArrayList<>();
        for (File eacFile : fileList) {
            if (!eacFile.getName().contains(integratedFileExtension)) {
                fileParsList.add(eacFile);
            }
        }

        HashMap<String, String> functionSubSetNames = new HashMap<>();
        try {
            ResultSet rs1 = dbHelper.getTableNameAndFunctionSubset(operatorName, systemType, measType);
            while (rs1.next()) {
                functionSubSetNames.put(rs1.getString("FUNCTIONSUBSET"), rs1.getString("TABLE_NAME"));
            }
            rs1.close();
        } catch (Exception e) {
        }

        ExecutorService executorForParser = Executors.newFixedThreadPool(numOfThreadParser);
        for (File fileObj : fileParsList) {
            String tableName = null;
            if (fileObj.getName().contains("GSM_Board")) {
                if (fileObj.getName().contains(".6")) {
                    tableName = "RANINFO_DOGU_GSM_BOARD";
                } else {
                    tableName = "RANINFO_BATI_GSM_BOARD";
                }
            } else if (fileObj.getName().contains("GSMBTS_Board")) {
                if (fileObj.getName().contains(".6")) {
                    tableName = "RANINFO_DOGU_GSMBTS_BOARD";
                } else {
                    tableName = "RANINFO_BATI_GSMBTS_BOARD";
                }

            } else {
                for (String func : functionSubSetNames.keySet()) {
                    if (fileObj.getName().contains(func)) {
                        tableName = functionSubSetNames.get(func);
                        break;
                    }
                }
            }

            String fileDate = fileObj.getName().split("\\_")[fileObj.getName().split("\\_").length - 1].substring(0, 8);

            if (fileObj.getName().endsWith(".csv") && tableName != null && fileObj.getName().contains("LTE_RANINFO")) {
                Runnable csvParseThread = new RanInfoNewCsvParser(fileObj, tableName, fileDate, OperationSystemEnum.REDHAT, ProgressTypeEnum.PRODUCT);
                executorForParser.execute(csvParseThread);
            } else if (fileObj.getName().endsWith(".csv") && tableName != null) {
                Runnable csvParseThread = new RanInfoCsvParser(fileObj, tableName, fileDate, OperationSystemEnum.REDHAT, ProgressTypeEnum.PRODUCT);
                executorForParser.execute(csvParseThread);
            } else if (fileObj.getName().endsWith(".xls") && tableName != null) {
                Runnable xlsParseThread = new RanInfoXlsParser(fileObj, tableName, fileDate, OperationSystemEnum.REDHAT, ProgressTypeEnum.PRODUCT);
                executorForParser.execute(xlsParseThread);
            } else {
                System.err.println("* TableName is null for " + fileObj.getName());
            }

        }

        executorForParser.shutdown();
        while (!executorForParser.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
        System.out.println("All cm files parsed");
    }

    @Override
    public void loaderProcedures() throws ProcedureException {
        //dbHelper.callFunctionAfterParserNoParameter("NORTHI_PARSER.RANINFO_GET_ATOLL_DATA", "Raninfo Get Atoll Data");
    }

    @Override
    public void objectProcedures() throws ProcedureException {
        for (String mailListStr : mailQueue.keySet()) {
            dbHelper.callProceduresAfterParser("NORTHI.SEND_MAIL('" + mailListStr + "','RANINFO - Missing Files','" + mailQueue.get(mailListStr) + "' )", "Send Mail Procedure");
        }
    }
}
