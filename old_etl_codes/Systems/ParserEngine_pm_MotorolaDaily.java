package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.MotorolaDailyProgress;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.MotorolaDailyProgress.CheckFilesLog;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.MotorolaDailyProgress.OpenWriterStream;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.MotorolaSplitCellStatistics;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import static com.ttgint.parserEngine.common.AbsParserEngine.systemType;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import java.io.File;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author EnesTerzi
 */
@ParserSystem(systemType = "MOTOROLA-DAILY", measType = "PM", operatorName = "VODAFONE")
public class ParserEngine_pm_MotorolaDaily extends AbsParserEngine {

    public static int FirstSplit;
    public static int SecondSplit;
    private static final HashSet<String> motorolaRawTableList = new HashSet<>();
    private static final ArrayList<String> motorolaOmcName = new ArrayList<>();
    private final String splitModel;
    public static HashMap<String, String> omcFolderIdMap = new HashMap<>();

    public ParserEngine_pm_MotorolaDaily(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
        this.splitModel = systemType.split("\\-")[1];
        AbsParserEngine.systemType = systemType.split("\\-")[0];

        try (ResultSet rSet1 = dbHelper.getOmcFolderId()) {
            while (rSet1.next()) {
                motorolaOmcName.add(rSet1.getString("ne_name"));
            }
        } catch (Exception ex) {
        }

        try (ResultSet rSet2 = dbHelper.getMotorolaRawTableNames()) {
            while (rSet2.next()) {

                String tablename = rSet2.getString("table_name");
                if (tablename.contains("ENTITY")) {
                    continue;
                }
                motorolaRawTableList.add(tablename);
            }
        } catch (Exception ex) {
        }

        try (ResultSet rSet3 = dbHelper.getOmcFolderId()) {
            while (rSet3.next()) {
                omcFolderIdMap.put(rSet3.getString("ne_name"), rSet3.getString("raw_ne_id"));
            }
        } catch (Exception ex) {
        }
    }

    @Override
    public void setProperties() {
    }

    @Override
    public void prepareParser() {
        try {
            splitFiles();
        } catch (IOException ex) {
            Logger.getLogger(ParserEngine_pm_MotorolaDaily.class.getName()).log(Level.SEVERE, null, ex);
        }

        Thread thr = new Thread(new MotorolaDailyProgress().new OpenWriterStream());
        thr.start();
        ExecutorService executeObj = Executors.newFixedThreadPool(1);
        for (String omcName : motorolaOmcName) {
            for (String tabLeName : motorolaRawTableList) {
                executeObj.execute(new MotorolaDailyProgress(omcName, tabLeName).new CheckFilesLog());
            }
        }
        executeObj.shutdown();
        while (!executeObj.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }

        MotorolaDailyProgress.CheckFilesLogJob = true;
        try {
            thr.join();
        } catch (InterruptedException ex) {
            Logger.getLogger(ParserEngine_pm_MotorolaDaily.class.getName()).log(Level.SEVERE, null, ex);
        }

        File[] filelist = new File(AbsParserEngine.LOCALFILEPATH).listFiles();
        for (File eachFile : filelist) {
            if (eachFile.getName().contains("+")) {
                try {
                    java.nio.file.Files.delete(eachFile.toPath());
                } catch (IOException ex) {
                    Logger.getLogger(ParserEngine_pm_MotorolaDaily.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

    }

    private boolean splitFiles() throws IOException {
        System.out.println("Split Progress Started at " + CommonLibrary.get_CurrentDatetime("yyyy-MM-dd HH:mm:ss"));
        File[] fileList = new File(AbsParserEngine.LOCALFILEPATH).listFiles();
        ExecutorService executorSplitFiles = Executors.newFixedThreadPool(1);
        Runnable splitobj;
        int[] SplitRange = {0, 600, 1332};
        for (String omcname : motorolaOmcName) {
            for (File currentFile : fileList) {
                if (!currentFile.getName().contains("cell_statistics_1")
                        && !currentFile.getName().contains("cell_statistics_2")
                        && !currentFile.getName().contains("cell_statistics_3")
                        && currentFile.getName().contains("cell_statistics")
                        && currentFile.getName().contains(omcname)) {

                    splitobj = new MotorolaSplitCellStatistics(currentFile, splitModel, omcFolderIdMap, SplitRange);
                    executorSplitFiles.execute(splitobj);
                }
            }
        }

        executorSplitFiles.shutdown();
        while (!executorSplitFiles.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
        System.out.println("Split Progress Finished at " + CommonLibrary.get_CurrentDatetime("yyyy-MM-dd HH:mm:ss"));

        return false;
    }

    @Override
    public void loaderProcedures() throws ProcedureException {
        dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(1)", "2G Loader");
    }

    @Override
    public void objectProcedures() throws ProcedureException {
    }

}
