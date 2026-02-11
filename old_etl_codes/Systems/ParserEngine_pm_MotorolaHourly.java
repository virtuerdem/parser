package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.MotorolaSplitCellStatistics;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import static com.ttgint.parserEngine.common.AbsParserEngine.systemType;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.nio.file.StandardCopyOption;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

@ParserSystem(systemType = "MOTOROLA-HOURLY", measType = "PM", operatorName = "VODAFONE")
public class ParserEngine_pm_MotorolaHourly extends AbsParserEngine {

    static int FirstSplit;
    static int SecondSplit;
    public static final HashMap<String, String> omcFolderIdMap = new HashMap<>();
    private final String splitModel;
    int parserRunningHour;
    int today;

    public ParserEngine_pm_MotorolaHourly(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
        this.splitModel = (systemType.split("\\-")[1]);
        AbsParserEngine.systemType = systemType.split("\\-")[0];

        Date date = new Date();   // given date
        Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
        calendar.setTime(date);   // assigns calendar to given date
        parserRunningHour = calendar.get(Calendar.HOUR_OF_DAY);
        today = calendar.get(Calendar.DAY_OF_WEEK);

        try (ResultSet rSet1 = dbHelper.getOmcFolderId()) {
            while (rSet1.next()) {
                omcFolderIdMap.put(rSet1.getString("ne_name"), rSet1.getString("raw_ne_id"));
            }
        } catch (Exception ex) {
            Logger.getLogger(ParserEngine_pm_MotorolaHourly.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public final void setProperties() {

    }

    @Override
    public void prepareParser() {
        changeFileNamesInStart();

        entityFilesProgress();
        try {
            splitFiles();
            File[] fileListX = new File(AbsParserEngine.LOCALFILEPATH).listFiles();
            for (File currentFile : fileListX) {
                if (currentFile.getName().contains("cell_statistics")) {
                    java.nio.file.Files.delete(currentFile.toPath());
                }
                if (currentFile.getName().equals("CELL_STATISTICS" + AbsParserEngine.integratedFileExtension)) {
                    java.nio.file.Files.delete(currentFile.toPath());
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(ParserEngine_pm_MotorolaHourly.class.getName()).log(Level.SEVERE, null, ex);
        }

        File[] fileListX = new File(AbsParserEngine.LOCALFILEPATH).listFiles();
        for (File currentFile : fileListX) {
            if (currentFile.getName().endsWith(".txt")) {
                String omcName = currentFile.getName().split("\\+")[0];

                try (BufferedReader br = new BufferedReader(new FileReader(currentFile))) {

                    FileWriter fw = new FileWriter(changeFileName(currentFile), true);
                    PrintWriter pw = new PrintWriter(fw);
                    String line = null;

                    while ((line = br.readLine()) != null) {
                        ArrayList<String> counterValues = new ArrayList<String>(Arrays.asList(line.split("\\" + AbsParserEngine.resultParameter, -1)));
                        String networkId = counterValues.get(0);
                        counterValues.remove(0);
                        line = new BigInteger(networkId).add(new BigInteger("1000000000").multiply(new BigInteger(omcFolderIdMap.get(omcName)))) + AbsParserEngine.resultParameter
                                + CommonLibrary.joinStringFromList(counterValues, AbsParserEngine.resultParameter) + omcName;
                        pw.println(line);

                    }
                    pw.close();
                    br.close();
                    java.nio.file.Files.delete(currentFile.toPath());

                } catch (IOException ex) {
                    Logger.getLogger(ParserEngine_pm_MotorolaHourly.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void entityFilesProgress() {
        File[] filelist = new File(AbsParserEngine.LOCALFILEPATH).listFiles();
        for (File eachFile : filelist) {
            if (eachFile.getName().endsWith(".txt") && eachFile.getName().contains("entity")) {
                String omcName = eachFile.getName().split("\\+")[0];
                String[] splittedFilename = eachFile.getName().split("\\_");
                String year = splittedFilename[1];
                String time = splittedFilename[2].substring(0, 2);
                String fragmantDate = year.replace(".", "-") + " " + time + ":00";

                BufferedWriter writer;
                try {
                    BufferedReader reader = new BufferedReader(new FileReader(eachFile));
                    writer = new BufferedWriter(new FileWriter(changeFileName(eachFile), true));
                    String line = null;
                    while ((line = reader.readLine()) != null) {

                        line = line + "0";

                        String[] splittedLine = line.split("\\|");
                        splittedLine[splittedLine.length - 1] = fragmantDate;

                        String newLine = "";
                        if (splittedLine.length >= 6) {
                            if (!splittedLine[5].isEmpty()) {
                                splittedLine[5] = splittedLine[5].substring(0, splittedLine[5].length() - 3);
                            }
                            if (!splittedLine[6].isEmpty()) {
                                splittedLine[6] = splittedLine[6].substring(0, splittedLine[6].length() - 3);
                            }
                            // System.out.println(splittedLine[5]);
                            for (String x : splittedLine) {
                                newLine += x + "|";
                            }
                            newLine = newLine.substring(0, newLine.length() - 1);
                            ArrayList<String> counterValues = new ArrayList<String>(Arrays.asList(newLine.split("\\" + AbsParserEngine.resultParameter, -1)));
                            String networkId = counterValues.get(0);
                            String parentId = counterValues.get(1);
                            counterValues.remove(0);
                            counterValues.remove(0);
                            newLine = new BigInteger(networkId).add(new BigInteger("1000000000").multiply(new BigInteger(omcFolderIdMap.get(omcName)))) + AbsParserEngine.resultParameter
                                    + new BigInteger(parentId).add(new BigInteger("1000000000").multiply(new BigInteger(omcFolderIdMap.get(omcName)))) + AbsParserEngine.resultParameter
                                    + CommonLibrary.joinStringFromList(counterValues, AbsParserEngine.resultParameter) + AbsParserEngine.resultParameter + omcName;
                            writer.write(newLine + "\n");
                        }
                    }
                    reader.close();
                    writer.close();
                    java.nio.file.Files.delete(eachFile.toPath());

                } catch (IOException ex) {
                    Logger.getLogger(ParserEngine_pm_MotorolaHourly.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    private void changeFileNamesInStart() {
        File[] fileListX = new File(AbsParserEngine.LOCALFILEPATH).listFiles();
        for (File currentFile : fileListX) {
            if (currentFile.getName().endsWith(".unl") && currentFile.getName().contains("_20")) {
                try {
                    File newFile = new File(currentFile.getAbsolutePath().replace(".unl", ".txt"));
                    java.nio.file.Files.move(currentFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException ex) {
                    Logger.getLogger(ParserEngine_pm_MotorolaHourly.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public File changeFileName(File fileThatChange) {
        File file = null;
        int index = 0;
        String[] spliteedFilename = fileThatChange.getName().split("\\+");
        String omcName = spliteedFilename[0];
        String fileName = spliteedFilename[1];
        index = fileName.indexOf("20");
        String edittedFileName = fileName.substring(0, index - 1);
        String cuttedDate = fileName.substring(index, fileName.indexOf(".txt"));
        cuttedDate = cuttedDate.replace(".", "").replace("_", "") + "00";
        String newFileName = edittedFileName.toUpperCase() + AbsParserEngine.integratedFileExtension;
        file = new File(AbsParserEngine.LOCALFILEPATH + newFileName);
        return file;
    }

    private void splitFiles() throws IOException {
        System.out.println("Split Progress Started at " + CommonLibrary.get_CurrentDatetime("yyyy-MM-dd HH:mm:ss"));
        File[] fileList = new File(AbsParserEngine.LOCALFILEPATH).listFiles();
        //Pool sayisi arttirilinca olusturulan file'larda
        //hatalar olusuyor.
        ExecutorService executorSplitFiles = Executors.newFixedThreadPool(1);
        Runnable splitobj;
        int[] splitRange = {0, 600, 1332};
        for (File currentFile : fileList) {
            if (currentFile.getName().contains("cell_statistics") && currentFile.getName().endsWith(".txt")) {
                splitobj = new MotorolaSplitCellStatistics(currentFile, splitModel, omcFolderIdMap, splitRange);
                executorSplitFiles.execute(splitobj);
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
        try {
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ParserEngine_pm_MotorolaHourly.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void loaderProcedures() throws ProcedureException {
    }

    @Override
    public void objectProcedures() throws ProcedureException {
        if (parserRunningHour % 4 == 0) {
            System.out.println("Waiting loader process...");
            int minute = 0;
            while (true) {
                Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
                minute = calendar.get(Calendar.MINUTE);

                if (minute > 35) {
                    dbHelper.callProceduresAfterParser("NORTHI.OBJECTS_PROCESS", "Motorola Object Proc");
                    dbHelper.callProceduresAfterParser("NORTHI_PARSER_SETTINGS.OBJECTS_MOTOROLA_ELEMT_UPDATE", "Motorola Element update Procedure");
                    break;
                }
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                }
            }
        }
    }
}
