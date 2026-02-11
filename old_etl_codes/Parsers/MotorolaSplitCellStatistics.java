/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 *
 * @author TTGParserTeam
 */
public class MotorolaSplitCellStatistics implements Runnable {

    private final File file;
    private int column;
    private int raw;
    private final int[] splitRange;
    private final String splitModel;

    private final ArrayList<BufferedWriter> BuffWriter = new ArrayList<>();
    private final String omcName;
    public final HashMap<String, String> omcFolderIdMap;

    public MotorolaSplitCellStatistics(File file, String SplitModel, HashMap<String, String> omcFolderIdMap, int... SplitRange) throws IOException {
        this.file = file;
        this.splitRange = SplitRange;
        this.splitModel = SplitModel;
        omcName = file.getName().split("\\+")[0];
        this.omcFolderIdMap = omcFolderIdMap;

    }

    private void doAction(int[][] x) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line = null;
            String[] splittedline = null;
            while ((line = reader.readLine()) != null) {
                if (line.endsWith("||")) {
                    line = line + "0";
                    splittedline = line.split("\\|");
                    splittedline[splittedline.length - 1] = "";
                } else {
                    splittedline = line.split("\\|");
                }
                sendLineToBuffers(splittedline, x);
            }
            reader.close();
        }
        closeAllBuffers();
    }

    private void sendLineToBuffers(String[] line, int[][] x) throws IOException {
        String id = null;
        String date = null;
        String fragmantdate = null;
        for (int i = 0; i < column; i++) {
            int firstSplit = x[i][0];
            int secondSplit = x[i][1];
            String writedLine = null;
            for (int a = firstSplit; a < secondSplit; a++) {
                if (a == 0) {
                    id = line[0];
                    date = line[1];
                    fragmantdate = line[2];
                }
                if (i == 0) {
                    writedLine = writedLine + line[a] + "|";
                }
                if (i > 0) {
                    if (a == firstSplit) {
                        writedLine = writedLine + id + "|" + date + "|" + fragmantdate + "|";
                    }
                    writedLine = writedLine + line[a] + "|";
                }

            }
            writedLine = writedLine.replace("null", "");
            ArrayList<String> counterValues = new ArrayList<String>(Arrays.asList(writedLine.split("\\" + AbsParserEngine.resultParameter, -1)));
            String networkId = counterValues.get(0);
            counterValues.remove(0);
            writedLine = new BigInteger(networkId).add(new BigInteger("1000000000").multiply(new BigInteger(omcFolderIdMap.get(omcName)))) + AbsParserEngine.resultParameter
                    + CommonLibrary.joinStringFromList(counterValues, AbsParserEngine.resultParameter) + omcName;

            BuffWriter.get(i).write(writedLine + "\n");

        }

    }

    private void closeAllBuffers() throws IOException {
        for (BufferedWriter eachWriter : BuffWriter) {
            eachWriter.close();
        }
    }

    private void createBuffers() throws IOException {
        for (int x = 0; x < column; x++) {
            if (splitModel.equalsIgnoreCase("DAILY")) {
                String[] splittedFilename = file.getName().split("\\+");
                String omcname = splittedFilename[0];
                String tableName = splittedFilename[1].substring(0, splittedFilename[1].length() - 4);
                File newFile = new File(AbsParserEngine.LOCALFILEPATH + omcname + "+" + tableName + "_" + (x + 1) + ".unl");
                BufferedWriter write = new BufferedWriter(new FileWriter(newFile, true));
                BuffWriter.add(write);
            } else if (splitModel.equalsIgnoreCase("HOURLY")) {
                String[] splittedFilename = file.getName().split("\\+");
                String[] splitteedDate = splittedFilename[1].split("\\_");
                String newFileNamee = splitteedDate[0].toUpperCase() + "_" + splitteedDate[1].toUpperCase() + "_" + (x + 1) + AbsParserEngine.integratedFileExtension;
                File newFile = new File(AbsParserEngine.LOCALFILEPATH + newFileNamee);
                BufferedWriter write = new BufferedWriter(new FileWriter(newFile, true));
                BuffWriter.add(write);
            }
        }
    }

    private int[][] cretaSplitRange(int... a) throws FileNotFoundException, IOException {
        int[][] b = new int[a.length + 1][2];
        column = 0;
        for (int i = 0; i < a.length; i++) {
            for (raw = 0; raw < 2; raw++) {
                if (raw == 0) {
                    b[column][raw] = a[i];
                } else if (raw == 1) {
                    if (i < a.length) {
                        if ((column + 1) == a.length) {
                            b[column][raw] = a[i];
                        } else {
                            b[column][raw] = a[i + 1];
                        }
                    } else {
                        b[column][raw] = a[i];
                    }
                }

            }
            column++;
        }
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line = null;

        String[] splittedline = null;
        while ((line = reader.readLine()) != null) {
            splittedline = line.split("\\|", -1);
            break;
        }
        reader.close();

        if (a[a.length - 1] != splittedline.length - 1) {
            b[a.length - 1][1] = splittedline.length - 1;
        }
        
        return b;
    }

    @Override
    public void run() {
        try {
            //System.out.println("Splitting = " + file);
            int[][] b = cretaSplitRange(splitRange);
            createBuffers();
            doAction(b);
        } catch (IOException e) {
        }
    }

}
