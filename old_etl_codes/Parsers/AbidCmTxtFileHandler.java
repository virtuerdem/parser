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
import com.ttgint.parserEngine.parserHandler.FileReaderHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;

/**
 *
 * @author ibrahimegerci
 */
public class AbidCmTxtFileHandler extends FileReaderHandler {

    private RawTableObject tableObject;
    private String tableColumnNames = "";
    private String outputFileName = "";
    private String fileContentDataDate;
    private String fileHeaderNames = "";
    private int rowCount = 0;

    public AbidCmTxtFileHandler(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void onStartParseOperation() {
        this.tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(currentFileProgress.getName().split("\\-", 2)[0]);
    }

    @Override
    public void lineProgress(String line) {
        rowCount++;
        try {
            if (rowCount == 1) {//header
                tableColumnNames = tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter);
                outputFileName = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;
                fileContentDataDate = currentFileProgress.getName().split("\\-", 2)[1].replace(".txt", "").replace("-", "");
                fileHeaderNames = "DATA_DATE" + AbsParserEngine.resultParameter + line.replace("|", AbsParserEngine.resultParameter);
            } else if (rowCount > 1 && this.tableObject != null) { //values
                String record = fileContentDataDate + AbsParserEngine.resultParameter + line.replace("|", AbsParserEngine.resultParameter);

                //prepare record
                for (int i = 0; i < fileHeaderNames.split("\\" + AbsParserEngine.resultParameter).length - record.split("\\" + AbsParserEngine.resultParameter).length; i++) {
                    record = record + AbsParserEngine.resultParameter;
                }
                record = CommonLibrary.get_RecordValue(fileHeaderNames.toUpperCase(), record, tableColumnNames.toUpperCase(), "", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter) + "\n";
                writeIntoFilesWithController(outputFileName, record);
            }
        } catch (Exception e) {
            System.err.println("*Parse Error " + e.getMessage() + " for " + currentFileProgress.getName() + " at " + rowCount + " value: " + line.replace("|", AbsParserEngine.resultParameter));
        }

    }

    @Override
    public void onstopParseOperation() {
    }

}
