/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.ttgint.parserEngine.common.AbsParserEngine;

/**
 *
 * @author EnesTerzi
 */
public class EricssonTextEmmParser implements Runnable {

    private final File currentFile;
    private String tablename;
    private String date;
    private String serverIp;

    public EricssonTextEmmParser(File currentFile) {
        this.currentFile = currentFile;
    }

    @Override
    public void run() {
        String[] splittedfileName = currentFile.getName().split("\\+");
        serverIp = splittedfileName[0].trim();
        splittedfileName = currentFile.getName().split("\\_");
        tablename = splittedfileName[3].split("\\.")[0].trim().toUpperCase();
        tablename = "EMM_" + tablename;
        date = splittedfileName[2].substring(0, splittedfileName[2].length() - 2);
        File outputFile = new File(AbsParserEngine.LOCALFILEPATH + tablename.toUpperCase().trim() + AbsParserEngine.integratedFileExtension);
        try {
            BufferedReader reader = new BufferedReader(new FileReader(currentFile));
            BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, true));
            String line = null;
            String newLine = "";
            while ((line = reader.readLine()) != null) {
                line = line.replace("'", "");
                String[] splittedLine = line.split("\\,");
                int counter = 0;
                for (String eachSplit : splittedLine) {
                    if (counter == 0) {
                        newLine = eachSplit.trim();
                    } else if (counter == 2) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
                        Calendar currentTime = Calendar.getInstance();
                        currentTime.setTime(sdf.parse(eachSplit));
                        currentTime.add(Calendar.HOUR, 1);
                        currentTime.set(Calendar.MINUTE, 0);
                        currentTime.set(Calendar.SECOND, 0);
                        newLine = newLine + "|" + sdf.format(currentTime.getTime());
                    } else {
                        newLine = newLine.trim() + "|" + eachSplit.trim();
                    }
                    counter++;
                }
                // System.out.println(newLine);
                writer.write(newLine + "\n");
            }

            reader.close();
            writer.close();
            java.nio.file.Files.delete(currentFile.toPath());
        } catch (IOException ex) {
            Logger.getLogger(EricssonTextEmmParser.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            Logger.getLogger(EricssonTextEmmParser.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

}
