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
import com.ttgint.parserEngine.parserHandler.CsvFileHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;

/**
 *
 * @author erdigurbuz
 */
public class RanInfoCsvParser extends CsvFileHandler {

    private String tableName;
    private String fullPath;
    private String fileContentDataDate;
    private int rowCount = 0;
    private RawTableObject tableObject;
    private String tableColumnNames;
    private String fileHeaderNames = "";

    public RanInfoCsvParser(File currentFileProgress, String tableName, String fileContentDataDate, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
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
    public void onstopParseOperation() {
        currentFileProgress.delete();
    }

    @Override
    public void lineProgress(String[] line) {
        String record = "";
        
        if (line.length < 2) {
            return;
        }

        if (rowCount > 0) {

            String[] newLine;
            //Csv sonuda  bos deger varsa sonuna date eklenecegi icin son degerin yerine ekleniyor
            if (line[line.length - 1].isEmpty()) {
                line[line.length - 1] = fileContentDataDate;
                for (int i = 0; i < line.length; i++) {
                    line[i] = line[i].trim();
                    if (" ".equals(line[i])) {
                        line[i] = "";
                    }
                }
                newLine = line;
                //Csv sonunda bos deger yok ise date yeni dizi yaratilip sonuna ekleniyor
            } else {
                //FileContentDataDate ekleniyor
                String[] updatedLine = new String[line.length + 1];
                System.arraycopy(line, 0, updatedLine, 0, line.length);
                updatedLine[updatedLine.length - 1] = fileContentDataDate;
                newLine = updatedLine;
            }

            //Trim ve bos karakter'ler null yapiliyor
            for (int i = 0; i < newLine.length; i++) {
                newLine[i] = newLine[i].trim();
                if (" ".equals(newLine[i])) {
                    newLine[i] = "";
                }
            }

            try {
                record = CommonLibrary.joinString(newLine, AbsParserEngine.resultParameter);
                record = CommonLibrary.get_RecordValue(fileHeaderNames, record, tableColumnNames, "", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter) + "\n";
                writeIntoFilesWithController(fullPath, record);
            } catch (Exception ex) {
                System.out.println(tableName + " - " + fullPath);
                System.out.println(ex.getMessage() + ": " + CommonLibrary.joinString(newLine, AbsParserEngine.resultParameter));
                System.out.println(fileHeaderNames);
                System.out.println(record);
                System.out.println(tableColumnNames);
            }

            rowCount++;
        } else {
            String[] newLine;
            //Csv sonuda  bos deger varsa sonuna date eklenecegi icin son degerin yerine ekleniyor
            if (line[line.length - 1].isEmpty()) {
                line[line.length - 1] = "DATA_DATE";
                for (int i = 0; i < line.length; i++) {
                    line[i] = line[i].trim();
                    if (" ".equals(line[i])) {
                        line[i] = "";
                    }
                }
                newLine = line;
                //Csv sonunda bos deger yok ise date yeni dizi yaratilip sonuna ekleniyor
            } else {
                //FileContentDataDate ekleniyor
                String[] updatedLine = new String[line.length + 1];
                System.arraycopy(line, 0, updatedLine, 0, line.length);
                updatedLine[updatedLine.length - 1] = "DATA_DATE";
                newLine = updatedLine;
            }
            fileHeaderNames = CommonLibrary.joinString(newLine, AbsParserEngine.resultParameter);
            rowCount++;
        }
    }

}
