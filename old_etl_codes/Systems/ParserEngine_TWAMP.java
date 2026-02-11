/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.TwampNewCsvFileHandler;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.common.ParserSystems;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ParserEngineException;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@ParserSystems(system = {
    @ParserSystem(systemType = "TWAMP", measType = "PM", operatorName = "VODAFONE"),
    @ParserSystem(systemType = "TWAMP", measType = "PM", operatorName = "KKTC-TELSIM")
})
public class ParserEngine_TWAMP extends AbsParserEngine {

    public ParserEngine_TWAMP(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);

    }

    public final static Set<String> dateList = Collections.synchronizedSet(new HashSet<String>());

    @Override
    public void setProperties() throws ParserEngineException {
    }

    @Override
    public void prepareParser() throws ParserEngineException {
        ArrayList<File> fileList = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);
        System.out.println("file size " + fileList.size());
        ExecutorService exeServer = Executors.newFixedThreadPool(AbsParserEngine.numOfThreadParser);
        for (File each : fileList) {
            if (each.getName().endsWith(AbsParserEngine.integratedFileExtension)) {
                continue;
            }
            TwampNewCsvFileHandler t1 = new TwampNewCsvFileHandler(each, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
            exeServer.execute(t1);
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
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.HOUR, -1);
        Date dd = cal.getTime();
        String todayBeforeOneHour = new SimpleDateFormat("yyyyMMddHH").format(dd);

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        Date date = calendar.getTime();
        String today = new SimpleDateFormat("yyyyMMddHH").format(date);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(new Date());
        Date dd2 = cal2.getTime();
        String todayQuarterMinute = parserDate(Integer.parseInt(new SimpleDateFormat("mm").format(dd2)), today, todayBeforeOneHour);

        if (AbsParserEngine.operatorName.equals("VODAFONE")) {
            dbHelper.callProceduresAfterParser(String.format("NORTHI_LOADER.P_TWAMP_PERF(TO_DATE('%s','YYYYMMDDHH24MI'))", todayQuarterMinute), "P_TWAMP_PERF Loader(TO_DATE('" + todayQuarterMinute + "','YYYYMMDDHH24MI'))");
            dbHelper.callProceduresAfterParser(String.format("NORTHI_LOADER.P_TWAMP_PERF_SUB_REGION(TO_DATE('%s','YYYYMMDDHH24MI'))", todayQuarterMinute), "P_TWAMP_PERF_SUB_REGION Loader(TO_DATE('" + todayQuarterMinute + "','YYYYMMDDHH24MI'))");
            dbHelper.callProceduresAfterParser(String.format("NORTHI_LOADER.P_TWAMP_PERF_MAIN_REGION(TO_DATE('%s','YYYYMMDDHH24MI'))", todayQuarterMinute), "P_TWAMP_PERF_MAIN_REGION Loader(TO_DATE('" + todayQuarterMinute + "','YYYYMMDDHH24MI'))");
            dbHelper.callProceduresAfterParser(String.format("NORTHI_LOADER.P_TWAMP_PERF_ILCE(TO_DATE('%s','YYYYMMDDHH24MI'))", todayQuarterMinute), "P_TWAMP_PERF_ILCE Loader(TO_DATE('" + todayQuarterMinute + "','YYYYMMDDHH24MI'))");
            dbHelper.callProceduresAfterParser(String.format("NORTHI_LOADER.P_TWAMP_PERF_CITY(TO_DATE('%s','YYYYMMDDHH24MI'))", todayQuarterMinute), "P_TWAMP_PERF_CITY Loader(TO_DATE('" + todayQuarterMinute + "','YYYYMMDDHH24MI'))");
            dbHelper.callProceduresAfterParser(String.format("NORTHI_LOADER.P_TWAMP_PERF_NW(TO_DATE('%s','YYYYMMDDHH24MI'))", todayQuarterMinute), "P_TWAMP_PERF_NW Loader(TO_DATE('" + todayQuarterMinute + "','YYYYMMDDHH24MI'))");
            dbHelper.callProceduresAfterParser(String.format("NORTHI_LOADER.P_TWAMP_PERF_15MIN(TO_DATE('%s','YYYYMMDDHH24MI'))", todayQuarterMinute), "P_TWAMP_PERF_15MIN Loader(TO_DATE('" + todayQuarterMinute + "','YYYYMMDDHH24MI'))");

            if (todayQuarterMinute.endsWith("45")) {
                dbHelper.callProceduresAfterParser(String.format("NORTHI_LOADER.P_TWAMP_PERF_H (TO_DATE('%s','YYYYMMDDHH24'))", todayBeforeOneHour), "P_TWAMP_PERF_H Loader(TO_DATE('" + todayBeforeOneHour + "','YYYYMMDDHH24MI'))");
                dbHelper.callProceduresAfterParser(String.format("NORTHI_LOADER.P_NNI_PERF(TO_DATE('%s','YYYYMMDDHH24'))", todayBeforeOneHour), "P_NNI_PERF(TO_DATE('" + todayBeforeOneHour + "','YYYYMMDDHH24MI'))");
                dbHelper.callProceduresAfterParser(String.format("NORTHI_LOADER.P_NNI_PERF_CITY(TO_DATE('%s','YYYYMMDDHH24'))", todayBeforeOneHour), "P_NNI_PERF_CITY(TO_DATE('" + todayBeforeOneHour + "','YYYYMMDDHH24MI'))");
                dbHelper.callProceduresAfterParser(String.format("NORTHI_LOADER.P_NNI_PERF_REGION(TO_DATE('%s','YYYYMMDDHH24'))", todayBeforeOneHour), "P_NNI_PERF_REGION(TO_DATE('" + todayBeforeOneHour + "','YYYYMMDDHH24MI'))");
                dbHelper.callProceduresAfterParser(String.format("NORTHI_LOADER.P_NNI_PERF_VENDOR(TO_DATE('%s','YYYYMMDDHH24'))", todayBeforeOneHour), "P_NNI_PERF_VENDOR(TO_DATE('" + todayBeforeOneHour + "','YYYYMMDDHH24MI'))");
                dbHelper.callProceduresAfterParser(String.format("NORTHI_LOADER.P_NNI_PERF_NW(TO_DATE('%s','YYYYMMDDHH24'))", todayBeforeOneHour), "P_NNI_PERF_NW(TO_DATE('" + todayBeforeOneHour + "','YYYYMMDDHH24MI'))");
                dbHelper.callProceduresAfterParser(String.format("NORTHI_LOADER.P_NNI_PERF_15MIN(TO_DATE('%s','YYYYMMDDHH24'))", todayBeforeOneHour), "P_NNI_PERF_15MIN(TO_DATE('" + todayBeforeOneHour + "','YYYYMMDDHH24MI'))");
                dbHelper.callProceduresAfterParser(String.format("NORTHI_LOADER.P_NNI_PERF_H(TO_DATE('%s','YYYYMMDDHH24'))", todayBeforeOneHour), "P_NNI_PERF_H(TO_DATE('" + todayBeforeOneHour + "','YYYYMMDDHH24MI'))");
            }
        } else {
            dbHelper.callProceduresAfterParser(String.format("NORTHI_LOADER.P_KKTC_TWAMP_PERF(TO_DATE('%s','YYYYMMDDHH24MI'))", todayQuarterMinute), "P_KKTC_TWAMP_PERF Loader(TO_DATE('" + todayQuarterMinute + "','YYYYMMDDHH24MI'))");
            dbHelper.callProceduresAfterParser(String.format("NORTHI_LOADER.P_KKTC_TWAMP_PERF_SUB_REGION(TO_DATE('%s','YYYYMMDDHH24MI'))", todayQuarterMinute), "P_KKTC_TWAMP_PERF_SUB_REGION Loader(TO_DATE('" + todayQuarterMinute + "','YYYYMMDDHH24MI'))");
            dbHelper.callProceduresAfterParser(String.format("NORTHI_LOADER.P_KKTC_TWAMP_PERF_NW(TO_DATE('%s','YYYYMMDDHH24MI'))", todayQuarterMinute), "P_KKTC_TWAMP_PERF_NW Loader(TO_DATE('" + todayQuarterMinute + "','YYYYMMDDHH24MI'))");
            dbHelper.callProceduresAfterParser(String.format("NORTHI_LOADER.P_KKTC_TWAMP_PERF_15MIN(TO_DATE('%s','YYYYMMDDHH24MI'))", todayQuarterMinute), "P_KKTC_TWAMP_PERF_15MIN Loader(TO_DATE('" + todayQuarterMinute + "','YYYYMMDDHH24MI'))");
        }
    }

    @Override
    public void objectProcedures() throws ProcedureException {
    }

    public static String parserDate(int minute, String today, String todayBeforeOneHour) {
        if (minute >= 6 && minute < 21) {
            return todayBeforeOneHour + "30";
        } else if (minute >= 21 && minute < 36) {
            return todayBeforeOneHour + "45";
        } else if (minute >= 36 && minute < 51) {
            return today + "00";
        } else if (minute >= 51 && minute <= 59) {
            return today + "15";
        } else if (minute >= 0 && minute < 6) {
            return todayBeforeOneHour + "30";
        }

        return null;
    }
}
