/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.exceptions.ParserEngineException;

/**
 *
 * @author erdigurbuz
 */
public class DecodeSgsnLogFile implements Runnable {

    private final File decodeFile;

    public DecodeSgsnLogFile(File decodeFile) {
        this.decodeFile = decodeFile;
    }

    @Override
    public void run() {

        try {
            ProcessBuilder pb = new ProcessBuilder("perl", "parse_ebm_log.pl", "-f" + decodeFile.getName(), "-o", AbsParserEngine.LOCALFILEPATH + decodeFile.getName()+".txt" , "-e", "att,act");
            pb.directory(new File(AbsParserEngine.LOCALFILEPATH + decodeFile.getName().split("/")[decodeFile.getName().split("/").length - 1]));
            Process p = pb.start();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            FileOutputStream fOut = new FileOutputStream(AbsParserEngine.LOCALFILEPATH + "decode.log", true);
            String line = null;
            while ((line = reader.readLine()) != null) {
                fOut.write((line + "\n").getBytes());
            }
            fOut.close();

            //File folder delete
            try {
                CommonLibrary.deleteFolderAndContent(new File(AbsParserEngine.LOCALFILEPATH + decodeFile.getName().split("/")[decodeFile.getName().split("/").length - 1]));
            } catch (ParserEngineException ex) {
            }
        } catch (Exception ex) {
            System.out.println("Decode process error: " + ex.getMessage());
        }
    }

}
