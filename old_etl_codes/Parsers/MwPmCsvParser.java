/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_csv_MW.subnetMap;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ParserIOException;
import com.ttgint.parserEngine.parserHandler.CsvFileHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.util.Arrays;

/**
 *
 * @author TurgutSimsek
 */
public class MwPmCsvParser extends CsvFileHandler {

    private String fileHeaderName;
    private String dataDate;
    private boolean startFlag = false;
    private RawTableObject tableObject;
    private String counterNameFileHeader;
    private String fileDate;

    public MwPmCsvParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void lineProgress(String[] line) {
        String editLine = "";
        for (String str : line) {
            editLine += str + "|";
        }

        editLine = editLine.substring(0, editLine.length() - 1);

        if (editLine.startsWith("Link Name")) {
            tableObject = TableWatcher.getInstance().getTableObjectFromCounterName(Arrays.asList(line));
            fileHeaderName = editLine;
            counterNameFileHeader = tableObject.getFullColumnOrderUsingCounterNameFil("|");

            String constant = "DATA_DATE|FILE_DATE|";

            fileHeaderName = constant + fileHeaderName + "|" + "NE_SUBNET" + "|" + "NE_SUBNET_PATH";
            startFlag = true;
            return;
        }
        if (editLine.startsWith("Save Time:")) {
            String[] time = editLine.split("\\:");
            dataDate = time[1];
            dataDate = dataDate.substring(0, 10);
            fileDate = time[1] + ":" + time[2] + ":" + time[3];

        }

        if (startFlag) {
            String[] sub = subnetMap.get(editLine.split("\\|")[2]);
            String subnet = null;
            String subnetPath = null;
            if (sub != null) {
                subnet = sub[0].isEmpty() ? null : sub[0];
                subnetPath = sub[1].isEmpty() ? null : sub[1];
            }

            editLine = dataDate + "|" + fileDate + "|" + editLine + "|" + (subnet == null ? "" : subnet) + "|" + (subnetPath == null ? "" : subnetPath);

            for (int i = 0; i < fileHeaderName.split("\\|").length - editLine.split("\\|").length; i++) {
                editLine = editLine + "|";
            }

            String result = CommonLibrary.get_RecordValue(fileHeaderName, editLine, counterNameFileHeader, "|", "|", "|");
            result += "\n";

            String fileName = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;

            try {
                writeIntoFilesWithController(fileName, result);
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
        currentFileProgress.delete();

    }

}
