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
import com.ttgint.parserEngine.parserHandler.CsvFileHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author turgut.simsek
 */
public class WdmCsvReportParser extends CsvFileHandler {

    private String dataDate;
    private RawTableObject tableObject;
    private boolean isValueReadStarted = false;
    private String fileHeader;

    public WdmCsvReportParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void lineProgress(String[] line) {

        if (line == null) {
            return;
        }

        if (!isValueReadStarted && line[0].startsWith("Save Time")) {
            try {
                //Save Time: 10/11/2018 11:34:35
                dataDate = line[0];
                dataDate = dataDate.split("Time:")[1];
                Date dateObject = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss").parse(dataDate);
                Calendar cal = Calendar.getInstance();
                cal.setTime(dateObject);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                dataDate = new SimpleDateFormat("yyyyMMddHHmmss").format(cal.getTime());
            } catch (ParseException ex) {
                System.out.println(ex);
            }
            return;
        }

        if (!isValueReadStarted && line[0] != null) {
            if (tableObject == null) {
                tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(line[0]);
            }
        }

        if (!isValueReadStarted && tableObject != null && tableObject.isExistCounterNameFileInRawTableObject(line[0])) {
            fileHeader = CommonLibrary.joinString(line, AbsParserEngine.resultParameter);
            fileHeader = "DATA_DATE" + AbsParserEngine.resultParameter + fileHeader;
            isValueReadStarted = true;
            return;
        }

        if (isValueReadStarted) {

            String record = CommonLibrary.joinString(line, AbsParserEngine.resultParameter);

            record = dataDate + AbsParserEngine.resultParameter + record;

            String resut = CommonLibrary.get_RecordValue(fileHeader, record, tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter), "0", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter);
            resut += "\n";
            String fileName = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;

            try {
                writeIntoFilesWithController(fileName, resut);
            } catch (ParserIOException ex) {
                ex.printStackTrace();
            }

        }

    }

    @Override
    public void onStartParseOperation() {
    }

    @Override
    public void onstopParseOperation() {
        currentFileProgress.deleteOnExit();
    }

}
