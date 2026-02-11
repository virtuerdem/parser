package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_MotorolaDaily;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author TTGParserTeam©
 */
public class MotorolaDailyProgress {

    private String omcName;
    private String tableName;
    public static boolean WriterStream = true;
    public static boolean CheckFilesLogJob = false;

    static Deque<Subset> subSetlist = new LinkedBlockingDeque<>();

    public MotorolaDailyProgress() {
        this(null, null);
    }

    public MotorolaDailyProgress(String omcName, String tableName) {
        this.omcName = omcName;
        this.tableName = tableName;

    }

    public class OpenWriterStream implements Runnable {

        @Override
        public void run() {
            boolean currentjob = true;
            CurrentJob:
            while (WriterStream) {
                while (!subSetlist.isEmpty() && currentjob) {
                    currentjob = false;
                    Subset currentSubsetObj = subSetlist.remove();
                    String[] splittedDate = currentSubsetObj.getDate().split(" ");
                    String omcName = currentSubsetObj.getOmcName();
                    String tableName = currentSubsetObj.getTableName().toUpperCase();
                    String date = splittedDate[0].replace("-", "") + splittedDate[1].replace(":", "");
                    String omcID = ParserEngine_pm_MotorolaDaily.omcFolderIdMap.get(omcName);
                    String newFileName = tableName + AbsParserEngine.integratedFileExtension;
                    try {
                        AbsParserEngine.dbHelper.deleteFromMotorolaRawTable(currentSubsetObj.getDate(), tableName, omcID, newFileName);
                    } catch (Exception ex) {
                        Logger.getLogger(MotorolaDailyProgress.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    File newFile = new File(AbsParserEngine.LOCALFILEPATH + newFileName);
                    try {
                        try (BufferedWriter writer = new BufferedWriter(new FileWriter(newFile, true))) {
                            try (BufferedReader reader = new BufferedReader(new FileReader(currentSubsetObj.getCurrentFile()))) {
                                String line = null;
                                while ((line = reader.readLine()) != null) {
                                    String[] splittedLine = line.split("\\|");
                                    if (splittedLine[2].equals(currentSubsetObj.getDate())) {
                                        if (AbsParserEngine.systemType.contains("-DAILY")) {
                                            if (tableName.contains("CELL_STATISTICS")) {
                                            } else {
                                                ArrayList<String> counterValues = new ArrayList<String>(Arrays.asList(line.split("\\" + AbsParserEngine.resultParameter, -1)));
                                                String networkId = counterValues.get(0);
                                                counterValues.remove(0);
                                                line = new BigInteger(networkId).add(new BigInteger("1000000000").multiply(new BigInteger(omcID))) + AbsParserEngine.resultParameter
                                                        + CommonLibrary.joinStringFromList(counterValues, AbsParserEngine.resultParameter) + omcName;
                                            }
                                        } else {
                                            ArrayList<String> counterValues = new ArrayList<String>(Arrays.asList(line.split("\\" + AbsParserEngine.resultParameter, -1)));
                                            String networkId = counterValues.get(0);
                                            counterValues.remove(0);
                                            line = new BigInteger(networkId).add(new BigInteger("1000000000").multiply(new BigInteger(omcID))) + AbsParserEngine.resultParameter
                                                    + CommonLibrary.joinStringFromList(counterValues, AbsParserEngine.resultParameter) + omcName;
                                        }

                                        writer.write(line + "\n");
                                    }
                                }
                                reader.close();
                            }
                            writer.close();

                        }

                    } catch (IOException ex) {
                        Logger.getLogger(MotorolaDailyProgress.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                currentjob = true;
                if (subSetlist.isEmpty() && CheckFilesLogJob) {
                    WriterStream = false;
                    break CurrentJob;
                }
            }
        }
    }

    public class CheckFilesLog implements Runnable {

        @Override
        public void run() {
            File currentFile = new File(AbsParserEngine.LOCALFILEPATH + omcName.toLowerCase() + "+" + tableName.toLowerCase() + ".unl");
            if (currentFile.exists()) {
                MotorolaTimePeriods[] periods = MotorolaTimePeriods.values();
                for (MotorolaTimePeriods timeperiods : periods) {
                    timeperiods.setOmcName(omcName);
                    timeperiods.setTableName(tableName);
                }
                BufferedReader reader;
                try {
                    reader = new BufferedReader(new FileReader(currentFile));
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        String[] splittedLine = line.split("\\|");
                        String dateofLine = splittedLine[2].split(" ")[1];
                        for (MotorolaTimePeriods time : periods) {
                            if (dateofLine.trim().equals(time.getTimes().trim())) {
                                time.setDate(splittedLine[2]);
                                time.getTimePeriodProp().count();
                                break;
                            }
                        }
                    }
                    reader.close();

                } catch (IOException e) {

                }

                for (MotorolaTimePeriods timePeriod : periods) {
                    try {
                        int loaded_data_count = 0;
                        ResultSet rss = null;
                        rss = AbsParserEngine.dbHelper.getLogsLoadeDDataCountForMotorola(timePeriod.getDate().trim(), timePeriod.getOmcName(), timePeriod.getTableName());
                        while (rss.next()) {
                            loaded_data_count = Integer.parseInt(rss.getString("LOADED_DATA_COUNT"));
                        }
                        rss.close();
                        if (loaded_data_count < timePeriod.getTimePeriodProp().getCount()) {
                            Subset subobj = new Subset(timePeriod.getDate(), timePeriod.getTableName(), timePeriod.getOmcName(), loaded_data_count, timePeriod.getTimePeriodProp().getCount(), currentFile);
                            subSetlist.add(subobj);
                        }
                        timePeriod.resetTimePeriodContext(); // ONEMLI !!!
                    } catch (SQLException e) {

                    } catch (Exception ex) {
                        Logger.getLogger(MotorolaDailyProgress.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }

        }

    }

}

class Subset {

    private final String date;
    private final String tableName;
    private final String OmcName;
    private final int DbLoadedCount;
    private final int FileCount;
    private final File currentFile;

    public Subset(String date, String tableName, String OmcName, int DbLoadedCount, int FileCount, File currentFile) {
        this.date = date;
        this.tableName = tableName;
        this.OmcName = OmcName;
        this.DbLoadedCount = DbLoadedCount;
        this.FileCount = FileCount;
        this.currentFile = currentFile;
    }

    public File getCurrentFile() {
        return currentFile;
    }

    public String getDate() {
        return date;
    }

    public String getTableName() {
        return tableName;
    }

    public String getOmcName() {
        return OmcName;
    }

    public int getDbLoadedCount() {
        return DbLoadedCount;
    }

    public int getFileCount() {
        return FileCount;
    }

}
