/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.RawCounterObject;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class TwampCsvFileHandler extends FileReaderHandler {

    private RawTableObject tableObject;
    String fileHeaderName = "";
    String tableColumnNames = "";

    public TwampCsvFileHandler(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    private int datePosition;
    boolean isLineStarted = false;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
    private final SimpleDateFormat sdfDb = new SimpleDateFormat("yyyyMMddHHmm");

    @Override
    public void onStartParseOperation() {
        String functionSubsetName = currentFileProgress.getName().split("\\_")[1];
        tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(functionSubsetName);
    }

    @Override
    public void lineProgress(String line) {

        if (tableObject != null) {
            String[] splitLineArray = line.split("\\,");

            if (splitLineArray[0].startsWith("TimeGroup")) {
                datePosition = 0;

                ArrayList<RawCounterObject> counterlist = tableObject.getCounterObjectList();

                for (RawCounterObject counterlist1 : counterlist) {
                    tableColumnNames += counterlist1.getCounterNameFile() + AbsParserEngine.resultParameter;
                }

                tableColumnNames = tableColumnNames.substring(0, tableColumnNames.length() - 1);

                for (String headers : splitLineArray) {
                    fileHeaderName += headers + AbsParserEngine.resultParameter;
                }
                fileHeaderName = fileHeaderName.substring(0, fileHeaderName.length() - 1);
                isLineStarted = true;

                return;
            }

            if (isLineStarted) {
                try {
                    if (sdf.format(sdf.parse(splitLineArray[datePosition])).equals(splitLineArray[datePosition])) {

                        String date = splitLineArray[datePosition];

                        Date editDate = sdf.parse(date);

                        date = sdfDb.format(editDate);
                        splitLineArray[datePosition] = date;

                        String fileoutputName = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;

                        if (splitLineArray[datePosition].contains(Integer.toString(Calendar.getInstance().get(Calendar.YEAR)))) {
                            line = String.join(AbsParserEngine.resultParameter, splitLineArray);
                            writeIntoFilesWithController(fileoutputName, CommonLibrary.get_RecordValue(fileHeaderName.toUpperCase(), line, tableColumnNames.toUpperCase(), "0", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter) + "\n");
                        }
                    }
                } catch (ParseException | ArrayIndexOutOfBoundsException ex) {
                } catch (ParserIOException ex) {
                }
            }
        }

    }

    @Override
    public void onstopParseOperation() {
    }

}
