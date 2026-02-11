package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.HW2G3GCSPmSaxXmlParser;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.SharedSitesParser;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.LOCALFILEPATH;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import static com.ttgint.parserEngine.common.AbsParserEngine.numOfThreadParser;
import static com.ttgint.parserEngine.common.AbsParserEngine.systemType;
import static com.ttgint.parserEngine.common.AbsParserEngine.vendorID;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.common.ParserSystems;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import com.ttgint.parserEngine.systemProperties.RanElementsInfo;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author TTGParserTeam©
 */
@ParserSystems(system = {
    @ParserSystem(systemType = "HW5G", measType = "PM", operatorName = "VODAFONE"),
    @ParserSystem(systemType = "HW4G", measType = "PM", operatorName = "VODAFONE"),
    @ParserSystem(systemType = "HW3G", measType = "PM", operatorName = "VODAFONE"),
    @ParserSystem(systemType = "HW2G", measType = "PM", operatorName = "VODAFONE"),
    @ParserSystem(systemType = "HWCS", measType = "PM", operatorName = "VODAFONE"),
    @ParserSystem(systemType = "HW3G", measType = "PM", operatorName = "KKTC-TELSIM"),
    @ParserSystem(systemType = "HW2G", measType = "PM", operatorName = "KKTC-TELSIM"),
    @ParserSystem(systemType = "HWCS", measType = "PM", operatorName = "KKTC-TELSIM")
})
public class ParserEngine_pm_HW_2g3gcs extends AbsParserEngine {

    public static HashMap<String, String> neNameAndIdList = new HashMap<>();
    public static final ConcurrentHashMap<String, String> rncNameTogeneratedIdMap = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, String> btsNameToBtsId = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, String> nodebNameToNodeBId = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, String> enodebNameToENodeBId = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, String> gnodebNameToGNodeBId = new ConcurrentHashMap<>();
    public static final ConcurrentHashMap<String, String> nodebNameRncId = new ConcurrentHashMap<>();

    public static int CELLCLASSTYPEID;
    public static int BTSCLASSTYPEID;
    public static int BSCCLASSTYPEID;
    public static int TRXCLASSTYPEID;
    public static ConcurrentHashMap<String, Boolean> cellCounter = new ConcurrentHashMap<>();

    public String topParentColumnName;
    int parserRunningHour;
    int today;

    public ParserEngine_pm_HW_2g3gcs(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(new Date());
        parserRunningHour = calendar.get(Calendar.HOUR_OF_DAY);
        today = calendar.get(Calendar.DAY_OF_WEEK);
    }

    @Override
    public final void setProperties() {
        CELLCLASSTYPEID = RanElementsInfo.CELL.getNeTypeId();
        BTSCLASSTYPEID = RanElementsInfo.BTSorNB.getNeTypeId();
        BSCCLASSTYPEID = RanElementsInfo.BSCorRNC.getNeTypeId();
        TRXCLASSTYPEID = RanElementsInfo.TRX.getNeTypeId();
    }

    @Override
    public void prepareParser() {
        switch (systemType) {
            case "HW3G":
                vendorID = 4;
                topParentColumnName = "RNCID";
                break;
            case "HW2G":
                vendorID = 5;
                topParentColumnName = "BSCID";
                break;
            case "HW4G":
                vendorID = 6;
                topParentColumnName = "NODEBID";
                break;
            case "HW5G":
                vendorID = 9;
                topParentColumnName = "NODEBID";
                break;
            case "HWCS":
                break;
        }

        try {
            ResultSet rSet2 = dbHelper.getNEIDandNameInfo(systemType);
            while (rSet2.next()) {
                neNameAndIdList.put(rSet2.getString("ne_name"), rSet2.getString("raw_ne_id"));
            }
            rSet2.close();
        } catch (Exception e) {
        }

        switch (systemType) {
            case "HW2G":
                try {
                ResultSet rss = AbsParserEngine.dbHelper.getActiveBscNameAndIdByLastDate(AbsParserEngine.operatorName);
                while (rss.next()) {
                    String neId = rss.getString("NE_ID");
                    String dataDate = rss.getString("DATA_DATE");
                    ResultSet rss2 = AbsParserEngine.dbHelper.getActiveBtsNameAndId(AbsParserEngine.operatorName, dataDate, neId);
                    while (rss2.next()) {
                        String btsName = rss2.getString("NE_NAME");
                        String btsId = rss2.getString("NE_ID");
                        if (btsNameToBtsId.containsKey(btsName) == false) {
                            btsNameToBtsId.put(btsName, btsId);
                        }
                    }
                    rss2.close();
                }
                rss.close();
            } catch (Exception ex) {

            }
            break;
            case "HW3G":
            case "HW4G":
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
                            nodebNameRncId.put(nodebName, neId);
                        }
                    }
                    rss2.close();
                }
                rss.close();
            } catch (Exception ex) {

            }

            if (AbsParserEngine.systemType.equals("HW4G")) {
                try {
                    ResultSet rss = AbsParserEngine.dbHelper.getActiveEnodbNameAndIdByLastDate(AbsParserEngine.operatorName);
                    while (rss.next()) {
                        String neName = rss.getString("NE_NAME");
                        String neId = rss.getString("NE_ID");
                        enodebNameToENodeBId.put(neName, neId);
                    }

                } catch (Exception ex) {
                }
            }
            break;
            case "HW5G":
                try {
                ResultSet rss = AbsParserEngine.dbHelper.getActiveGnodbNameAndIdByLastDate(AbsParserEngine.operatorName);
                while (rss.next()) {
                    String neName = rss.getString("NE_NAME");
                    String neId = rss.getString("NE_ID");
                    gnodebNameToGNodeBId.put(neName, neId);
                }

            } catch (Exception ex) {
            }
            break;
        }

        System.out.println("Number of Active Subset          : " + TableWatcher.getInstance().activeSubsetSize());
        System.out.println("Number of Active Counter         : " + TableWatcher.getInstance().activeCounterSize());
        System.out.println("Number of active Network Element : " + neNameAndIdList.size());

        ExecutorService executorForParser = Executors.newFixedThreadPool(numOfThreadParser);
        List<File> fileList = CommonLibrary.list_AllFilesAsFile(LOCALFILEPATH);

        for (File xmlFile : fileList) {
            if (xmlFile.getName().endsWith(".xml")) {
                Runnable xmlParseThread = null;
                xmlParseThread = new HW2G3GCSPmSaxXmlParser(xmlFile, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
                executorForParser.execute(xmlParseThread);

            } else if (xmlFile.getName().startsWith("VDF_LTE")) {
                Runnable csvParseThread = null;
                csvParseThread = new SharedSitesParser(xmlFile, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
                executorForParser.execute(csvParseThread);
            }
        }

        executorForParser.shutdown();

        while (!executorForParser.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
    }

    @Override
    public void loaderProcedures() throws ProcedureException {
        if ("HW5G".equals(systemType) && operatorName.equals("VODAFONE")) {
            dbHelper.callProceduresAfterParserWithThread("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(32)", "5G Loader");

        } else if ("HW4G".equals(systemType) && operatorName.equals("VODAFONE")) {
            dbHelper.callProceduresAfterParserWithThread("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(21)", "4G Loader");

        } else if ("HW3G".equals(systemType) && operatorName.equals("VODAFONE")) {
            boolean flag = false;
            for (String distinctDataDate : AbsParserEngine.loadedDatesUnieqeList.keySet()) {
                if (distinctDataDate.endsWith("30")) {
                    flag = true;
                }
            }
            //Yarım saatlik data islemede calistirma laoder'i
            if (flag) {
                dbHelper.callProceduresAfterParserWithThread("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(2)", "3G Loader");
            }
        } else if ("HW2G".equals(systemType) && operatorName.equals("VODAFONE")) {
            dbHelper.callProceduresAfterParserWithThread("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(1)", "2G Loader");

        } else if ("HWCS".equals(systemType) && operatorName.equals("VODAFONE")) {
            dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(35)", "HWCS Loader");
            
        } else if ("HW4G".equals(systemType) && operatorName.equals("KKTC-TELSIM")) {
            dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(34)", "KKTC 4G Loader");

        } else if ("HW3G".equals(systemType) && operatorName.equals("KKTC-TELSIM")) {
            dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(14)", "KKTC 3G Loader");

        } else if ("HW2G".equals(systemType) && operatorName.equals("KKTC-TELSIM")) {
            dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(13)", "KKTC 2G Loader");

        } else if ("HWCS".equals(systemType) && operatorName.equals("KKTC-TELSIM")) {
            dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(15)", "HWCS NSS LOADER");
            dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(16)", "HWCS SGSN LOADER");
            dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(17)", "HWCS GGSN LOADER");
            dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(37)", "HWCS CORE LOADER");
        }

        //PM Cell sayisi alinip loglaniyor
        HashMap<String, Integer> tmp = new HashMap<>();
        for (String data : cellCounter.keySet()) {
            String date = data.split("\\|")[0];
            Integer i = tmp.get(date);
            if (i == null) {
                tmp.put(date, 1);
            } else {
                tmp.put(date, ++i);
            }
        }
        for (String date : tmp.keySet()) {
            try {
                dbHelper.insertCellCount(date, tmp.get(date));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

    }

    @Override
    public void objectProcedures() throws ProcedureException {

    }
}
