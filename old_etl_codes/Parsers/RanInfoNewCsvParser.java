/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.io.File;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.exceptions.FileHandlerException;
import com.ttgint.parserEngine.parserHandler.CsvFileHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;

/**
 *
 * @author ibrahimegerci
 */
public class RanInfoNewCsvParser extends CsvFileHandler {

    private String tableName;
    private String fileContentDataDate;
    private RawTableObject tableObject;
    private String tableColumnNames = "";
    private String outputFileName = "";
    private String fileHeaderNames = "";
    private int rowCount = 0;

    public RanInfoNewCsvParser(File currentFileProgress, String tableName, String fileContentDataDate, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
        this.tableName = tableName;
        this.fileContentDataDate = fileContentDataDate;
    }

    @Override
    public void onStartParseOperation() {
        this.tableObject = TableWatcher.getInstance().getTableObjectFromTableName(tableName);
        this.tableColumnNames = tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter);
        this.outputFileName = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;
    }

    @Override
    public void lineProgress(String[] line) {
        rowCount++;
        try {
            if (rowCount > 1) { //clean data                
                for (int i = 0; i < line.length; i++) {
                    line[i] = line[i].replace("\t", " ").replace("\n", " ").trim();
                }
            }

            if (rowCount == 2) { //header
                //prepare header
                fileHeaderNames = "DATA_DATE" + AbsParserEngine.resultParameter + CommonLibrary.joinString(line, AbsParserEngine.resultParameter);
            } else if (rowCount > 2 && tableObject != null) { //values
                //prepare record
                String record = fileContentDataDate + AbsParserEngine.resultParameter + CommonLibrary.joinString(line, AbsParserEngine.resultParameter);
                for (int i = 0; i < fileHeaderNames.split("\\" + AbsParserEngine.resultParameter).length - record.split("\\" + AbsParserEngine.resultParameter).length; i++) {
                    record = record + AbsParserEngine.resultParameter;
                }
                record = CommonLibrary.get_RecordValue(fileHeaderNames, record, tableColumnNames, "", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter) + "\n";
                writeIntoFilesWithController(outputFileName, record);
            }
        } catch (Exception e) {
            System.err.println("*Parse Error " + e.toString() + " for " + currentFileProgress.getName() + " at " + rowCount + " value: " + CommonLibrary.joinString(line, AbsParserEngine.resultParameter));
        }
    }

    @Override
    public void onstopParseOperation() {
        try {
            deleteFile(currentFileProgress);
        } catch (FileHandlerException ex) {
            System.err.println("* " + currentFileProgress.getName() + " Delete error! " + ex.toString());
        }
    }

}
