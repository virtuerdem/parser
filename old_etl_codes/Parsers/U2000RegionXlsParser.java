/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.io.File;
import jxl.Cell;
import jxl.Sheet;
import com.ttgint.parserEngine.parserHandler.XlsFileHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;

/**
 *
 * @author TTGETERZI
 */
public class U2000RegionXlsParser extends XlsFileHandler {

    public U2000RegionXlsParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void onStartParseOperation() {

    }

    @Override
    public void parseSheets(Sheet[] sheets) {
        Sheet sheet = sheets[0];
        int totalRows = sheet.getRows();

        for (int i = 8; i < totalRows; i++) {
            try {
                Cell deviceName = sheet.getCell(0, i);
                Cell subnet = sheet.getCell(11, i);
                System.out.println(deviceName.getContents() + "|" + subnet.getContents());
            } catch (Exception e) {

            }
        }

    }

    @Override
    public void onstopParseOperation() {

    }
}
