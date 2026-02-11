/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.FileHandlerException;
import com.ttgint.parserEngine.parserHandler.CsvFileHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 *
 * @author ibrahimegerci
 */
public class FTTSCsvParser extends CsvFileHandler {

    private RawTableObject tableObject;
    private String tableColumnNames;
    private int lineCount = 0;
    private String fileName;
    private String fileDate;
    private String fullPath;
    private String fileHeaderNames;
    private ArrayList<Integer> dateMap = new ArrayList<>();

    public FTTSCsvParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
        fileName = currentFileProgress.getName();
    }

    @Override
    public void onStartParseOperation() {
        tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(fileName.substring(0, fileName.length() - 18));
        tableColumnNames = tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter);
        fullPath = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;
        fileDate = fileName.substring(fileName.length() - 17, fileName.length() - 9) + "000000";
    }

    @Override
    public void lineProgress(String[] line) {
        lineCount++;

        if (lineCount == 1) {
            fileHeaderNames = "DATA_DATE" + AbsParserEngine.resultParameter + CommonLibrary.joinString(line, AbsParserEngine.resultParameter);

            for (int i = 0; i < line.length; i++) {
                if (line[i].endsWith("DATE")) {
                    dateMap.add(i);
                }
            }
            return;
        }

        try {
            for (int i = 0; i < line.length; i++) {
                if (dateMap.contains(i)) {
                    try {
                        line[i] = new SimpleDateFormat("yyyyMMddHHmmss").format(new SimpleDateFormat("MM/dd/yyyy hh:mm:ss aaa").parse(line[i]));
                    } catch (ParseException ex) {
                    }
                }
            }

            String record = fileDate + AbsParserEngine.resultParameter + CommonLibrary.joinString(line, AbsParserEngine.resultParameter);
            record = CommonLibrary.get_RecordValue(fileHeaderNames, record, tableColumnNames, "", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter);
            writeIntoFilesWithController(fullPath, record + "\n");
        } catch (Exception ex) {
            System.err.println(fileName + "\n"
                    + " Writer - Error Line:" + lineCount + "\n"
                    + " FileHeader: " + fileHeaderNames + "\n"
                    + " Record    : " + line + "\n"
                    + ex.getMessage()
            );
        }
    }

    @Override
    public void onstopParseOperation() {
        try {
            deleteFile(currentFileProgress);
        } catch (FileHandlerException ex) {
            System.err.println("Corrupted File (deleted): " + currentFileProgress.getName() + ": " + ex.getMessage());
            try {
                deleteFile(getCurrentFileProgress());
            } catch (Exception e) {
            }
        }
    }
}
