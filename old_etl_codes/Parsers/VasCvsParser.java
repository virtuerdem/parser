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
import com.ttgint.parserEngine.exceptions.ParserIOException;
import com.ttgint.parserEngine.parserHandler.FileReaderHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;

/**
 *
 * @author abdullah.yakut
 */
public class VasCvsParser extends FileReaderHandler {

    private RawTableObject tableObject;
    private String tableColumnNames;
    private String fileHeaderNames;
    private ProgressTypeEnum progType;
    private int currentLine = 0;

    public VasCvsParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void onStartParseOperation() {
        String[] filenames = currentFileProgress.getName().replace(".csv", "").split("_");
        String functionSubsetName = "";
        for (int a = 1; a < filenames.length; a++) {
            functionSubsetName = functionSubsetName + filenames[a] + "_";
        }
        functionSubsetName = functionSubsetName.substring(0, functionSubsetName.length() - 1);
        tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(functionSubsetName);
        this.tableColumnNames = tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter);

    }

    @Override
    public void lineProgress(String line) {
        if (tableObject != null) {
            if (line.isEmpty() || line.contains("----") || line.length() < 5) {
                return;
            }
            String record = "";
            if (line.contains("||','||")) {
                fileHeaderNames = line.replaceAll("\\|\\|\\'\\,\\'\\|\\|", AbsParserEngine.resultParameter).toUpperCase();
            } else {
                record = line.replace(",", AbsParserEngine.resultParameter);

                String fullPath = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;
                try {
                    writeIntoFilesWithController(fullPath, CommonLibrary.get_RecordValue(fileHeaderNames, record, tableColumnNames, "0", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter) + "\n");
                } catch (ParserIOException ex) {

                }
            }

        }
    }

    @Override
    public void onstopParseOperation() {

    }

}
