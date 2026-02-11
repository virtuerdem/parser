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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author turgut.simsek
 */
public class WdmTxtParser extends FileReaderHandler {

    private int lineRowNumber = 0;
    private String fileFullHeader = "";
    private String dataDate = "";
    private String tableName = "";
    private RawTableObject tableObject;
    private String tableColumns;
    private SimpleDateFormat sdf;
    private String executionTime;

    public WdmTxtParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void onStartParseOperation() {
        if (currentFileProgress.getName().contains("WDM")) {
            tableName = "WDM_PFM";
        } else if (currentFileProgress.getName().contains("SDH")) {
            tableName = "SDH_PFM";
        }
        sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        dataDate = currentFileProgress.getName().split("\\_")[3].replace(".txt", "");
        dataDate = dataDate.substring(0, 10);
        try {
            Date date = new SimpleDateFormat("yyyyMMddHH").parse(dataDate);
            dataDate = sdf.format(date);
        } catch (ParseException ex) {
            Logger.getLogger(WdmTxtParser.class.getName()).log(Level.SEVERE, null, ex);
        }
        executionTime = sdf.format(new Date());
        tableObject = TableWatcher.getInstance().getTableObjectFromTableName(tableName);
        tableColumns = tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter);

    }

    @Override
    public void lineProgress(String line) {
        String[] lineArray = line.split("\\t");

        String record = CommonLibrary.joinString(lineArray, AbsParserEngine.resultParameter);
        record = record.substring(1, record.length() - 1);

        lineRowNumber++;
        if (lineRowNumber == 1) {
            record = record.replaceAll("\\ ", "");
            fileFullHeader = "DATA_DATE" + AbsParserEngine.resultParameter + "EXECUTION_TIME" + AbsParserEngine.resultParameter + record + AbsParserEngine.resultParameter + "END_TIME";
            return;
        }

        //EndTime date format
        String endTime = "";
        for (String col : record.split("\\" + AbsParserEngine.resultParameter)) {
            if (col.startsWith("20") && col.contains("+0")) {
                Date d = new Date();
                try {
                    d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(col.substring(0, col.indexOf("+")));
                } catch (ParseException ex) {
                }
                endTime = new SimpleDateFormat("yyyyMMddHHmmss").format(d) + AbsParserEngine.resultParameter;
                break;
            }
        }
        record = dataDate + AbsParserEngine.resultParameter + executionTime + AbsParserEngine.resultParameter + record + AbsParserEngine.resultParameter + endTime;
        writeIntoFiles(record);

    }

    private void writeIntoFiles(String record) {

        String myLine = CommonLibrary.get_RecordValue(fileFullHeader, record, tableColumns, "0", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter);
        String fileOutputName = AbsParserEngine.LOCALFILEPATH + tableName + AbsParserEngine.integratedFileExtension;

        try {
            writeIntoFilesWithController(fileOutputName, myLine + "\n");
        } catch (ParserIOException ex) {
            System.out.println(ex);
        }

    }

    @Override
    public void onstopParseOperation() {
    }

}
