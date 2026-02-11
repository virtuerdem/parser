/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.exceptions.ParserIOException;
import com.ttgint.parserEngine.parserHandler.FileReaderHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;

/**
 *
 * @author TTGETERZI
 */
public class NecPmonFileHandler extends FileReaderHandler {

    public NecPmonFileHandler(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    public static final String necTableName = "NEC_PMON_REPORT";
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy");
    private final SimpleDateFormat fileOutput = new SimpleDateFormat("yyyyMMdd");

    @Override
    public void onStartParseOperation() {

    }
    int counter = 0;

    @Override
    public void lineProgress(String line) {
        if (counter == 0) {
            counter++;
            return;
        }
        String newLineWithPipe = line.replace(",", "|");
        String[] splittedLine = newLineWithPipe.split("\\|");
        String dateString = splittedLine[splittedLine.length - 1];
        Date date;
        try {
            date = sdf.parse(dateString);
        } catch (ParseException ex) {
            return;
        }
        if (newLineWithPipe.contains("TRAKYA")) {
            newLineWithPipe = newLineWithPipe.replace("TRAKYA", "ISTANBUL TRAKYA");
        }
        String fileOutputName = AbsParserEngine.LOCALFILEPATH + necTableName + "-" + fileOutput.format(date) + AbsParserEngine.integratedFileExtension;
        try {
            writeIntoFilesWithController(fileOutputName, newLineWithPipe + "\n");
        } catch (ParserIOException ex) {
//            Logger.getLogger(NecPmonFileHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    @Override
    public void onstopParseOperation() {

    }

}
