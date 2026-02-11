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
import java.text.SimpleDateFormat;
import java.util.HashSet;
import net.openhft.hashing.LongHashFunction;

/**
 *
 * @author ibrahimegerci
 */
public class HwU2020AlarmCsvParser extends CsvFileHandler {

    private RawTableObject tableObject;
    private boolean isValueReadStarted = false;
    private String fileHeader;
    private String fileId;
    private String connectionId;
    private String functionSubsetName;
    private String dataDate;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
    private SimpleDateFormat sdfZ = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss Z");
    private HashSet<Integer> replaceTimes = new HashSet<>();
    private int locationInformationIndex = -1;

    public HwU2020AlarmCsvParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
        String[] splitted = currentFileProgress.getName().split("\\-");
        fileId = splitted[splitted.length - 1].split("\\.")[0];
        connectionId = splitted[1];
        dataDate = splitted[2].substring(0, 8) + "000000";
        functionSubsetName = currentFileProgress.getName()
                .replace(splitted[0] + "-", "")
                .replace(splitted[1] + "-", "")
                .replace(splitted[2] + "-", "")
                .replace("-" + splitted[splitted.length - 1], "");
    }

    @Override
    public void lineProgress(String[] line) {

        if (line == null) {
            return;
        }

        if (!isValueReadStarted) {
            for (int i = 0; i < line.length; i++) {
                switch (line[i]) {
                    case "OccurrenceTime":
                    case "arrivedUtcTime":
                    case "AcknowledgementTime":
                    case "ClearanceTime":
                        replaceTimes.add(i);
                        break;
                    case "LocationInformation":
                        locationInformationIndex = i;
                        break;
                }
            }
            tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(functionSubsetName);
            fileHeader = "DATA_DATE" + AbsParserEngine.resultParameter
                    + "CONNECTION_ID" + AbsParserEngine.resultParameter
                    + "FILE_ID" + AbsParserEngine.resultParameter
                    + CommonLibrary.joinString(line, AbsParserEngine.resultParameter);
            isValueReadStarted = true;
            return;
        }

        if (tableObject == null) {
            return;
        }

        if (isValueReadStarted) {
            for (int i : replaceTimes) {
                try {
                    line[i] = sdf.format(sdfZ.parse(line[i]));
                } catch (Exception e) {
                }
            }

            for (int i = 0; i < line.length; i++) {
                line[i] = line[i].replace("\t", " ").replace("\n", " ").replace("'", " ").replace("\"", " ").trim();
            }

            StringBuilder stringBuilderKeys = new StringBuilder();
            stringBuilderKeys.append(fileHeader);

            StringBuilder stringBuilderValues = new StringBuilder();
            stringBuilderValues.append(dataDate).append(AbsParserEngine.resultParameter)
                    .append(connectionId).append(AbsParserEngine.resultParameter)
                    .append(fileId).append(AbsParserEngine.resultParameter)
                    .append(CommonLibrary.joinString(line, AbsParserEngine.resultParameter));

            for (int i = 0; i < stringBuilderKeys.toString().split("\\" + AbsParserEngine.resultParameter).length - stringBuilderValues.toString().split("\\" + AbsParserEngine.resultParameter).length; i++) {
                stringBuilderValues.append(AbsParserEngine.resultParameter);
            }

            try {
                if (locationInformationIndex >= 0) {
                    //ObjectName= GL41_K2175_IzmitPlajYolu/eNodeB Function Name=EN41_54721_K2175_IzmitPlajYolu, Local Cell ID=81, Cell Name=LK217515O415472181, ...
                    //Cabinet No.=3, Subrack No.=7, Slot No.=0, Board Type=, Manager Port No.=1,....
                    for (String value : line[locationInformationIndex].split("\\, ")) {
                        try {
                            if (value.contains("/") && value.chars().filter(ch -> ch == '=').count() > 1) {
                                for (String subValue : value.split("\\/")) {
                                    try {
                                        stringBuilderKeys.append(AbsParserEngine.resultParameter + "LocationInformation_"
                                                + subValue.split("\\=", 2)[0].toString().trim().toLowerCase());
                                        stringBuilderValues.append(AbsParserEngine.resultParameter);
                                        stringBuilderValues.append(subValue.split("\\=", 2)[1].toString().trim());
                                    } catch (Exception e) {
                                    }
                                }
                            } else {
                                stringBuilderKeys.append(AbsParserEngine.resultParameter + "LocationInformation_"
                                        + value.split("\\=", 2)[0].toString().trim().toLowerCase());
                                stringBuilderValues.append(AbsParserEngine.resultParameter);
                                stringBuilderValues.append(value.split("\\=", 2)[1].toString().trim());
                            }
                        } catch (Exception e) {
                        }
                    }
                }
            } catch (Exception e) {
            }

            String result = CommonLibrary.get_RecordValue(stringBuilderKeys.toString(), stringBuilderValues.toString(), tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter), "", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter);
            result = String.valueOf(LongHashFunction.xx().hashChars(result)) + result + "\n";
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
        currentFileProgress.deleteOnExit();
    }

}
