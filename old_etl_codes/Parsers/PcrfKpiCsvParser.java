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
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author TurgutSimsek
 */
public class PcrfKpiCsvParser extends FileReaderHandler {

    private String fileHeaderNames;
    private String tableColumnNames;

    public PcrfKpiCsvParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    String DATA_DATE;
    String fileHeader;
    String fullPath;
    SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");
    SimpleDateFormat sdff = new SimpleDateFormat("yyyyMMddHHmm");
    RawTableObject tableObject;
    int fileHeaderNamesLenght=0;

    boolean flag = false;

    @Override
    public void lineProgress(String line) {

        String[] cells = line.split("\\,");

        if (cells[0].equals("Report Date")) {
            DATA_DATE = cells[1];
            try {
                DATA_DATE = sdff.format(sdf.parse(DATA_DATE));
            } catch (ParseException ex) {
                Logger.getLogger(PcrfKpiCsvParser.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (cells[0].equals("NE")) {

            tableObject = TableWatcher.getInstance().getTableObjectFromCounterName(Arrays.asList(cells));

            if (tableObject != null) {
                fileHeaderNames = "DATA_DATE|"+getRecordValue(cells);
                fileHeaderNamesLenght = fileHeaderNames.split("\\|").length;
                tableColumnNames = tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter);
                fullPath = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;
                flag = true;
                return;
            }
        }

        if (flag) {

            String record = DATA_DATE + "|" + getRecordValue(cells);
            try {
                String lines = CommonLibrary.get_RecordValue(fileHeaderNames, record, tableColumnNames, "0", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter) + "\n";
                writeIntoFilesWithController(fullPath,lines);

            } catch (ParserIOException ex) {
            }
        }
    }

    private String getRecordValue(String[] cells) {
        String lines = "";
        for (String cell : cells) {
            lines += cell + "|";
        }

        lines = lines.substring(0, lines.length() - 1);
        int i = lines.split("\\|").length;

        while (i < fileHeaderNamesLenght-1) { // Data_date colomunu elle eklediğimiz için 
            lines += "|0";
            ++i;
        }
        return lines;
    }

    @Override
    public void onStartParseOperation() {
    }

    @Override
    public void onstopParseOperation() {
    }

}
