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

public class DWDMCsvParser extends CsvFileHandler {

    private RawTableObject tableObject;
    private boolean isValueReadStarted = false;
    private String fileHeader;
    private String fileId;
    private String functionSubsetName;
    private String reportType;
    private int fromIndex;

    public DWDMCsvParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
        String[] splitted = currentFileProgress.getName().split("_");
        fileId = splitted[5];
        reportType = splitted[2];
        functionSubsetName = splitted[0] + "_" + splitted[1]; //dwdm_report

    }

    @Override
    public void lineProgress(String[] line) {
        if (line == null) {
            return;
        }

        if (!isValueReadStarted) {

            for (int i = 0; i < line.length; i++) {
                if ("From".equals(line[i])) {
                    fromIndex = i;
                }
            }

            tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(functionSubsetName);

            fileHeader = "DATA_DATE" + AbsParserEngine.resultParameter
                    + "FILE_ID" + AbsParserEngine.resultParameter
                    + "REPORT_TYPE" + AbsParserEngine.resultParameter
                    + CommonLibrary.joinString(line, AbsParserEngine.resultParameter);
            isValueReadStarted = true;
            return;
        }

        if (tableObject == null) {
            return;
        }

        if (isValueReadStarted) {

            String dataDate = line[fromIndex];

            //NS,NA,NotConfigured ignored
            for (int i = 0; i < line.length; i++) {
                if (line[i].contains("NS") || line[i].contains("NA") || line[i].contains("NotConfigured")) {
                    line[i] = line[i].replaceAll("[^0-9.]", "");
                }
            }

            String record = dataDate + AbsParserEngine.resultParameter
                    + fileId + AbsParserEngine.resultParameter
                    + reportType + AbsParserEngine.resultParameter
                    + CommonLibrary.joinString(line, AbsParserEngine.resultParameter);

            String result = CommonLibrary.get_RecordValue(fileHeader, record, tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter), "", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter);
            String fileOutputName = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;
            try {
                writeIntoFilesWithController(fileOutputName, result + "\n");

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
