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
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author ibrahimegerci
 */
public class HpCmTxtFileHandler extends FileReaderHandler {

    private RawTableObject tableObject;
    private String tableColumnNames = "";
    private String outputFileName = "";
    private String dataDate = "";
    private int rowCount = 0;
    private String nodeName = "";

    public HpCmTxtFileHandler(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void onStartParseOperation() {
        this.tableObject = TableWatcher.getInstance()
                .getTableObjectFromFunctionSubsetName(currentFileProgress.getName().split("\\.")[1].trim());
        tableColumnNames = tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter);
        outputFileName = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;
        dataDate = currentFileProgress.getName().split("\\_")[currentFileProgress.getName().split("\\_").length - 2];
        nodeName = currentFileProgress.getName().split("\\_")[currentFileProgress.getName().split("\\_").length - 3];
    }

    @Override
    public void lineProgress(String line) {
        rowCount++;
        try {
            StringBuilder header = new StringBuilder();
            StringBuilder record = new StringBuilder();

            header.append("dataDate")
                    .append(AbsParserEngine.resultParameter)
                    .append("auditTimeStampDate")
                    .append(AbsParserEngine.resultParameter)
                    .append("nodeName");

            record.append(dataDate)
                    .append(AbsParserEngine.resultParameter)
                    .append(new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(Long.valueOf(line.split("\\|", 2)[0]))))
                    .append(AbsParserEngine.resultParameter)
                    .append(nodeName);

            int i = 0;
            for (String cell : line.split("\\|")) {
                i++;
                header.append(AbsParserEngine.resultParameter).append(Integer.toString(i));
                record.append(AbsParserEngine.resultParameter).append(cell);
            }

            writeIntoFilesWithController(outputFileName,
                    CommonLibrary.get_RecordValue(
                            header.toString(),
                            record.toString(),
                            tableColumnNames,
                            "",
                            AbsParserEngine.resultParameter,
                            AbsParserEngine.resultParameter)
                    + "\n");
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
