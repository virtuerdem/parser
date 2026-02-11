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
public class HpKpiCmTxtFileHandler extends FileReaderHandler {

    private RawTableObject tableObject;
    private String tableColumnNames = "";
    private String outputFileName = "";
    private String dataDate = "";
    private int rowCount = 0;
    private String nodeName = "";
    private String headers;

    public HpKpiCmTxtFileHandler(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void onStartParseOperation() {
        this.tableObject = TableWatcher.getInstance()
                .getTableObjectFromFunctionSubsetName(currentFileProgress.getName().split("\\_", 3)[2].split("\\.")[0].trim());
        tableColumnNames = tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter);
        outputFileName = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;
        nodeName = currentFileProgress.getName().split("\\_")[0];
        dataDate = currentFileProgress.getName().split("\\_")[1];
    }

    @Override
    public void lineProgress(String line) {
        rowCount++;
        try {
            if (rowCount == 1) { //header
                headers = "dataDate"
                        + AbsParserEngine.resultParameter
                        + "nodeName"
                        + AbsParserEngine.resultParameter
                        + line.replace("|", AbsParserEngine.resultParameter);
            } else { //value
                String record
                        = dataDate
                        + AbsParserEngine.resultParameter
                        + nodeName
                        + AbsParserEngine.resultParameter
                        + line.replace("|", AbsParserEngine.resultParameter);

                writeIntoFilesWithController(
                        outputFileName,
                        CommonLibrary.get_RecordValue(
                                headers,
                                record,
                                tableColumnNames,
                                "",
                                AbsParserEngine.resultParameter,
                                AbsParserEngine.resultParameter
                        ) + "\n");
            }
        } catch (Exception e) {
            System.err.println("*Parse Error " + e.getMessage()
                    + " for " + currentFileProgress.getName()
                    + " at " + rowCount
                    + " value: " + line.replace("|", AbsParserEngine.resultParameter));
        }
    }

    @Override
    public void onstopParseOperation() {
    }

}
