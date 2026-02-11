/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.io.File;
import jxl.Cell;
import jxl.Sheet;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.exceptions.ParserIOException;
import com.ttgint.parserEngine.parserHandler.XlsFileHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;

/**
 *
 * @author erdigurbuz
 */
public class RanInfoXlsParser extends XlsFileHandler {

    private String tableName;
    private String fullPath;
    private String fileContentDataDate;
    private RawTableObject tableObject;
    private String tableColumnNames;
    private String fileHeaderNames = "";

    public RanInfoXlsParser(File currentFileProgress, String tableName, String fileContentDataDate, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
        this.tableName = tableName;
        this.fileContentDataDate = fileContentDataDate;
    }

    @Override
    public void onStartParseOperation() {
        fullPath = AbsParserEngine.LOCALFILEPATH + tableName + AbsParserEngine.integratedFileExtension;
        tableObject = TableWatcher.getInstance().getTableObjectFromTableName(tableName);
        this.tableColumnNames = tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter);
    }

    @Override
    public void parseSheets(Sheet[] sheets) {

        Sheet sheet = sheets[0];
        int totalRows = sheet.getRows();
        int firstIndex = 0;
        
        if (currentFileProgress.getName().contains("2G_TRX")) {
            firstIndex = 2;
        }

        for (int i = firstIndex; i < totalRows; i++) {
            //FileHeader
            if (i == 0 || (currentFileProgress.getName().contains("2G_TRX") && i == 2)) {
                String record = "";
                String cellData;
                Cell cell[] = sheet.getRow(i);
                for (int j = 0; j < cell.length; j++) {
                    cellData = cell[j].getContents().trim();
                    record += cellData + AbsParserEngine.resultParameter;
                }
                fileHeaderNames = record + "DATA_DATE";
            } else {
                String record = "";
                String cellData;
                Cell cell[] = sheet.getRow(i);
                for (int j = 0; j < cell.length; j++) {
                    cellData = cell[j].getContents().trim();
                    if (cellData.equals("<NULL>")) {
                        cellData = "";
                    }
                    record += cellData + AbsParserEngine.resultParameter;
                }
                record += fileContentDataDate + "\n";
                try {
                    record = CommonLibrary.get_RecordValue(fileHeaderNames, record, tableColumnNames, "", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter);
                    
                    writeIntoFilesWithController(fullPath, record);
                } catch (ParserIOException ex) {
                    System.out.println(ex.getMessage() + record);
                }
            }
        }
    }

    @Override
    public void onstopParseOperation() {
        currentFileProgress.delete();
    }

}
