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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author TurgutSimsek
 */
public class AluSdhCsvFileHandler extends FileReaderHandler {

    private final TableWatcher tableWtchrObj = TableWatcher.getInstance();
    private RawTableObject tableObject;
    String fileHeaderName = "";
    String tableColumnNames = "";

    public AluSdhCsvFileHandler(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    private final int datePosition = 5;

    boolean isLineStarted = false;
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
    private Date date;

    @Override
    public void lineProgress(String line) {
        String[] splitLineArray = line.split("\\" + AbsParserEngine.resultParameter);

        if (splitLineArray.length < 5) {
            return;
        }
        if (line.startsWith("TP_Object")) {
            tableObject = tableWtchrObj.getTableObjectFromCounterName(Arrays.asList(splitLineArray));
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

                    date = sdf.parse(splitLineArray[datePosition]);

                    String granPeriod = splitLineArray[1];
                    if (granPeriod.equals("15") == false) {
                        return;
                    }

                    String fileoutputName = AbsParserEngine.LOCALFILEPATH
                            + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;
                    line = 0 + line.replace(splitLineArray[datePosition], sdf.format(CommonLibrary.dateConverterToGmt(date)));

                    while (line.contains("||")) {
                        line = line.replace("||", "|0|");
                    }
                    splitLineArray = line.split("\\" + AbsParserEngine.resultParameter);
                    if (splitLineArray[datePosition].contains(Integer.toString(Calendar.getInstance().get(Calendar.YEAR)))) {
                        line = line.replace(splitLineArray[datePosition], sdf.format(CommonLibrary.dateConverterToGmt(date)));
                        if (line.split("\\" + AbsParserEngine.resultParameter).length == fileHeaderName.split("\\" + AbsParserEngine.resultParameter).length) {
                            writeIntoFilesWithController(fileoutputName, CommonLibrary.get_RecordValue(fileHeaderName.toUpperCase(), line, tableColumnNames.toUpperCase(), "0", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter) + "\n");
                        }

                    }
                }
            } catch (ParseException ex) {
            } catch (ParserIOException | ArrayIndexOutOfBoundsException ex) {
            }
        }
    }

    @Override
    public void onStartParseOperation() {
    }

    @Override
    public void onstopParseOperation() {
    }

}
