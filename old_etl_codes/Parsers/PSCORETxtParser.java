/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.io.File;

import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.parserHandler.FileReaderHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import com.ttgint.parserEngine.common.AbsParserEngine;

/**
 *
 * @author burakfircasiguzel
 */
public class PSCORETxtParser extends FileReaderHandler {

    private RawTableObject tableObject;
    private String tableColumnNames;
    private int lineCount = 0;
    private String fileName;
    private String fileDate;
    private String fullPath;
    private String fileHeaderNames;

    public PSCORETxtParser(File eachFile, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(eachFile, operationSystem, progType);
        fileName = eachFile.getName();
    }

    @Override
    public void onStartParseOperation() {
        tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(fileName.substring(0, fileName.length() - 21));
        tableColumnNames = tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter);
        fullPath = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;
        fileDate = fileName.substring(fileName.length() - 20, fileName.length() - 10).replace("-", "");
    }

    @Override
    public void lineProgress(String line) {
        lineCount++;

        if (lineCount == 1) {
            fileHeaderNames = "DATA_DATE" + AbsParserEngine.resultParameter
                    + line.replace(",", AbsParserEngine.resultParameter);
            return;
        }

        try {
            String record = fileDate + AbsParserEngine.resultParameter
                    + line.replace(",", AbsParserEngine.resultParameter).replace("\"", "");

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

    }

}
