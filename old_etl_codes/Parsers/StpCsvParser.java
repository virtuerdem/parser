/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.io.File;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ParserIOException;
import com.ttgint.parserEngine.parserHandler.CsvFileHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;

/**
 *
 * @author erdigurbuz
 */
public class StpCsvParser extends CsvFileHandler {

    private RawTableObject tableObject;
    private String tableColumnNames;
    private String fileHeaderNames;
    private ProgressTypeEnum progType;
    private int currentLine = 0;

    public StpCsvParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
        this.progType = progType;
    }

    @Override
    public void onStartParseOperation() {
        tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(
                currentFileProgress.getName().split("\\_")[currentFileProgress.getName().split("\\_").length - 1].replace(".csv", ""));
        if (tableObject != null) {
            this.tableColumnNames = tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter);
        }
    }

    @Override
    public void onstopParseOperation() {
        if (progType.equals(ProgressTypeEnum.PRODUCT)) {
            currentFileProgress.delete();
        }
    }

    @Override
    public void lineProgress(String[] line) {
        if (tableObject != null) {
            String record = CommonLibrary.joinString(line, AbsParserEngine.resultParameter);

            //File Header
            if (currentLine == 0) {
                fileHeaderNames = record.replace("Date_UTC|Time_UTC", "DATA_DATE");
            } else {
                String fileContentDataDate = line[0] + " " + line[1].replace("+0", "");
                String objectName = tableObject.getFunctionSubsetName() + "." + line[2];
                String granularity = line[3];
                String neName = line[4].trim();
                record = fileContentDataDate + AbsParserEngine.resultParameter + objectName + AbsParserEngine.resultParameter + granularity + AbsParserEngine.resultParameter + neName + AbsParserEngine.resultParameter;
                for (int i = 5; i < line.length; i++) {
                    record += line[i] + AbsParserEngine.resultParameter;
                }
                record = record.substring(0, record.length() - 1);

                String fullPath = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;
                try {
                    writeIntoFilesWithController(fullPath, CommonLibrary.get_RecordValue(fileHeaderNames, record, tableColumnNames, "0", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter) + "\n");
                } catch (ParserIOException ex) {

                }
            }

        }
        currentLine++;
    }

}
