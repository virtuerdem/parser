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
import java.util.LinkedHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author turgut.simsek
 */
public class WdmCfgTxtParser extends FileReaderHandler {

    private RawTableObject tableObject;
    boolean counterReadStart = false;
    LinkedHashMap<String, String> counterKeyValueMap;
    private String dataDate;
    private String fileDate;

    public WdmCfgTxtParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void onStartParseOperation() {
    }

    @Override
    public void lineProgress(String line) {

        if (!line.isEmpty()) {

            if (line.contains("Created by")) {
                try {
                    //2018-10-07 23:15:12 Created by 3.2.1.0
                    line = line.replace("//", "");
                    line = line.split("Created")[0];
                    line = line.trim();

                    Date fDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(line);
                    fileDate = new SimpleDateFormat("yyyyMMddHHmmss").format(fDate);

                    line = line.substring(0, 11);
                    dataDate = line.trim();
                    Date date = new SimpleDateFormat("yyyy-MM-dd").parse(dataDate);
                    dataDate = new SimpleDateFormat("yyyyMMddHHmmss").format(date);

                    return;
                } catch (ParseException ex) {
                    Logger.getLogger(WdmCfgTxtParser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            line = line.replace("{", "");
            line = line.replace("}", "");
            line = line.replace("\"", "");
            line = line.trim();

            if (line.contains(":cfg-")) {

                String counters = "";

                if (counterKeyValueMap != null && !counterKeyValueMap.isEmpty()) {
                    if (tableObject != null) {
                        writeIntoFiles();
                        counterKeyValueMap = null;
                    }
                }

                line = line.replace(":cfg-", "");
                String[] lineTopArr = line.split("\\:");
                String functionSubsetName = lineTopArr[0];
                counters = lineTopArr[1];

                tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(functionSubsetName);

                if (tableObject != null) {
                    counterKeyValueMap = new LinkedHashMap<>();
                    fillCounterKeyValueMap(counters);
                    return;
                }

            }

            if (tableObject != null) {
                fillCounterKeyValueMap(line);
            }

        }

    }

    void fillCounterKeyValueMap(String line) {

        for (String counterKeyValue : line.split("\\,")) {

            try {
                if (counterKeyValue.contains("=")) {
                    String[] counterKeyValueArr = counterKeyValue.split("\\=");
                    String counterName = "";
                    String counterValue = "0";

                    if (counterKeyValueArr.length == 2) {
                        counterName = counterKeyValueArr[0];
                        counterValue = counterKeyValueArr[1];
                    } else {
                        counterName = counterKeyValueArr[0];
                    }

                    counterKeyValueMap.put(counterName.trim(), counterValue.trim());
                }

            } catch (ArrayIndexOutOfBoundsException e) {
                System.out.println(counterKeyValue + "   -> " + e);
            }
        }
    }

    private void writeIntoFiles() {

        String fullFileHeader = "DATA_DATE" + AbsParserEngine.resultParameter + "FILE_DATE" + AbsParserEngine.resultParameter + getFileHeaderNames();
        String record = dataDate + AbsParserEngine.resultParameter + fileDate + AbsParserEngine.resultParameter + getCounterValues();
        String tableColumns = tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter);

        String myLine = CommonLibrary.get_RecordValue(fullFileHeader, record, tableColumns, "0", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter);
        String fileOutputName = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;

        try {
            writeIntoFilesWithController(fileOutputName, myLine + "\n");
        } catch (ParserIOException ex) {
            System.out.println(ex);
        }

    }

    private String getFileHeaderNames() {
        String fileHeaderNames = "";
        for (String key : counterKeyValueMap.keySet()) {
            fileHeaderNames += key + AbsParserEngine.resultParameter;
        }
        return fileHeaderNames.substring(0, fileHeaderNames.length() - 1);
    }

    private String getCounterValues() {
        String row = "";
        for (String value : counterKeyValueMap.values()) {
            row += value + AbsParserEngine.resultParameter;
        }

        return row.substring(0, row.length() - 1);
    }

    @Override
    public void onstopParseOperation() {
    }

}
