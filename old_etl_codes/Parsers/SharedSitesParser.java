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
 * @author ErdiGUrbuz
 */
public class SharedSitesParser extends FileReaderHandler {

    private String fileHeaderNames;
    private String tableColumnNames;

    public SharedSitesParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    RawTableObject tableObject;
    int currentLine = 0;

    public void onStartParseOperation() {
         tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(currentFileProgress.getName().split("\\__")[0]);
        if (tableObject != null) {
            this.tableColumnNames = tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter);
        }
    }

    @Override
    public void lineProgress(String line) {
        if (tableObject != null) {
            if (line.isEmpty()) {
                return;
            }
            
            line = line.replaceAll(",", AbsParserEngine.resultParameter).replaceAll("\"", "");
            
            //File Header
            if (currentLine == 0) {
                fileHeaderNames = line;
            } else {
                String fullPath = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;
                try {
                    writeIntoFilesWithController(fullPath, CommonLibrary.get_RecordValue(fileHeaderNames, line, tableColumnNames, "0", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter) + "\n");
                } catch (ParserIOException ex) {

                }
            }

        }
        currentLine++;
    }


    @Override
    public void onstopParseOperation() {
    }

}
