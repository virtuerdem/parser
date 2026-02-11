/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_csv_MW.subnetMap;
import com.ttgint.parserEngine.parserHandler.CsvFileHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ParserIOException;

/**
 *
 * @author TurgutSimsek
 */
public class MwPmCsvRegionParser extends CsvFileHandler {

    int subnetIndex = -1;
    int subnetPathIndex = -1;
    int neNameIndex = -1;
    private String fileHeaderName;
    private String dataDate;
    private boolean startFlag = false;
    private RawTableObject tableObject;
    private String counterNameFileHeader;
    private String fileDate;

    public MwPmCsvRegionParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void lineProgress(String[] line) {
        String editLine = "";
        for (String str : line) {
            editLine += str + "|";
        }
        editLine = editLine.substring(0, editLine.length() - 1);

        if (editLine.startsWith("NE Name")) {
            for (String counterHeader : line) {
                subnetIndex++;
                if (counterHeader.equals("Subnet")) {
                    break;
                }
            }
            for (String counterHeader : line) {
                subnetPathIndex++;
                if (counterHeader.equals("Subnet Path")) {
                    break;
                }
            }
            for (String counterHeader : line) {
                neNameIndex++;
                if (counterHeader.equals("NE Name")) {
                    break;
                }
            }
            tableObject = TableWatcher.getInstance().getTableObjectFromTableName("MW_LINK_REPORT_NE");
            fileHeaderName = editLine;
            counterNameFileHeader = tableObject.getFullColumnOrderUsingCounterNameFil("|");
            String constant = "DATA_DATE|FILE_DATE|";
            fileHeaderName = constant + fileHeaderName;
            startFlag = true;
            return;
        }
        if (subnetIndex > -1) {
            subnetMap.put(line[neNameIndex], new String[] {line[subnetIndex], line[subnetPathIndex]});
        }

        if (editLine.startsWith("Save Time:")) {
            String[] time = editLine.split("\\:");
            dataDate = time[1];
            dataDate = dataDate.trim().substring(0, 10);
            fileDate = time[1] + ":" + time[2] + ":" + time[3];
        }

        if (startFlag) {
            editLine = dataDate + "|" + fileDate.trim() + "|" + editLine;
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
