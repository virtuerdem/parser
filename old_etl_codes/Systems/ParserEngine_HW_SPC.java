/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.exceptions.ProcedureException;

/**
 *
 * @author TTGParserTeam©
 */
@ParserSystem(systemType = "HW" ,measType = "SPC",operatorName = "VODAFONE")
public class ParserEngine_HW_SPC extends AbsParserEngine {
    
    private static final String table_name = "LIST_BSC_RNC_SPC";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    File newFile = new File(AbsParserEngine.LOCALFILEPATH + table_name + AbsParserEngine.integratedFileExtension);

    public ParserEngine_HW_SPC(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }
    
  
    @Override
    public void setProperties() {
        try {
            AbsParserEngine.dbHelper.forSPCtruncateTableLIST_BSC_RNC_SPC();
        } catch (Exception ex) {
            Logger.getLogger(ParserEngine_HW_SPC.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    @Override
    public void prepareParser() {
        try {
            File file = new File(AbsParserEngine.LOCALFILEPATH + "spc.txt");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            ArrayList<String> fileContext = new ArrayList<>();
            String line = null;
            while ((line = reader.readLine()) != null) {
                fileContext.add(line);
            }
            reader.close();
            file.delete();
            BufferedWriter writer = new BufferedWriter(new FileWriter(newFile));
            for (String eachline : fileContext) {
                char[] chara = eachline.toCharArray();
                String bscName = "";
                String SPC = "";
                boolean swapWord = false;
                for (char a : chara) {
                    if ((int) a == 9) {
                        swapWord = true;
                    }
                    if (!swapWord && (int) a != 9) {
                        bscName = bscName + String.valueOf(a);
                    }
                    if (swapWord && (int) a != 9) {
                        SPC = SPC + String.valueOf(a);
                    }
                }
                int spcint = Integer.parseInt(SPC);
                String spcBinnary = Integer.toBinaryString(spcint);
                writer.write(bscName + "|" + SPC + "|" + com.ttgint.parserEngine.commonLibrary.CommonLibrary.binaryToHex(spcBinnary, 8) + "\n");
            }
            writer.close();

            //java.nio.file.Files.move(file.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(ParserEngine_HW_SPC.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(ParserEngine_HW_SPC.class.getName()).log(Level.SEVERE, null, ex);
        } catch (Exception ex) {
            Logger.getLogger(ParserEngine_HW_SPC.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    @Override
    public void loaderProcedures() throws ProcedureException {
    }

    @Override
    public void objectProcedures() throws ProcedureException {
    }
    
}