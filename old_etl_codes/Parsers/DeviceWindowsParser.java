/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.RawCounterObject;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.parserHandler.CsvFileHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.util.Arrays;

/**
 *
 * @author TURGUT SIMSEK
 */
public class DeviceWindowsParser extends CsvFileHandler {

    static boolean status = false;
    private TableWatcher tableWtchrObj = TableWatcher.getInstance();
    private RawTableObject tableObject;
    String fileHeaderName = "";
    String record = "";
    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    String tableColumnNames = "";
    String fileDate = "";
    private int j = 0;

    public DeviceWindowsParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void onStartParseOperation() {

    }

    @Override
    public void onstopParseOperation() {
        currentFileProgress.delete();
    }

    @Override
    public void lineProgress(String[] line) {

        if (line.length + 1 == tableWtchrObj.activeCounterSize()) {
            int i = 0;
            record += fileDate + "|";
            for (String fileLine : line) {
                if (fileLine.contains("%")) {
                    record += fileLine.split("\\%")[0] + "|";
                } else {
                    record += fileLine + "|";
                }
                if (fileLine.isEmpty() || fileLine.equals("")) {
                    i++; // dosyadki gelen satırdaki kolanlarda 3 ve  fazlasında null değer geldiyse o satırda counter datası yoktur
                }
                status = i <= 3;
            }
        } else {
            if (j == 1) {
                fileDate = line[j];
                String[] fileDateDizi = fileDate.split(" ")[0].split("\\/");
                fileDate = fileDateDizi[2] + fileDateDizi[0] + fileDateDizi[1];
                try {
                    fileDate = sdf.format(sdf.parse(fileDate));
                } catch (ParseException ex) {
                    Logger.getLogger(DeviceWindowsParser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            j++;
        }

        if (status) {

            if (tableColumnNames.equals("")) {
                tableObject = tableWtchrObj.getTableObjectFromCounterName(Arrays.asList(line));
                if (tableObject != null) {

                    ArrayList<RawCounterObject> counterList = tableObject.getCounterObjectList();
                    for (RawCounterObject counterList1 : counterList) {
                        tableColumnNames += counterList1.getCounterNameFile() + "|";
                    }

                }
                fileHeaderName += "DATA_DATE|";
                for (String headerLine : line) {
                    fileHeaderName += headerLine + "|";
                }

            }

            if (!record.contains("PercentUp")) {

                try {

                    String fullPath = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;
                    writeIntoFilesWithController(fullPath, CommonLibrary.get_RecordValue(fileHeaderName.toUpperCase(), record, tableColumnNames.toUpperCase(), "", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter) + "\n");
                } catch (Exception ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
        record = "";
    }
}
