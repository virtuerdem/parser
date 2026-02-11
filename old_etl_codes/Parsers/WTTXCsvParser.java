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
import java.util.HashSet;
import net.openhft.hashing.LongHashFunction;

/**
 *
 * @author ibrahimegerci
 */
public class WTTXCsvParser extends CsvFileHandler {

    private RawTableObject tableObject;
    private boolean isValueReadStarted = false;
    private String fileHeader;
    private String fileId;
    private String functionSubsetName;
    private int esnIndex;
    private int tacIndex;
    private int ecgiIndex;
    private int collectTimeIndex;
    private int manufacturerIndex;
    private HashSet<Integer> numberSet;

    public WTTXCsvParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
        String[] splitted = currentFileProgress.getName().split("_");
        fileId = splitted[1].replace(".csv", "");
        functionSubsetName = splitted[0];
    }

    @Override
    public void lineProgress(String[] line) {

        if (line == null) {
            return;
        }

        if (!isValueReadStarted) {
            numberSet = new HashSet<>();
            for (int i = 0; i < line.length; i++) {
                line[i] = line[i].replace("\t", " ").replace("\n", " ").trim();
                switch (line[i]) {
                    case "ESN":
                        esnIndex = i;
                        break;
                    case "ECGI":
                        ecgiIndex = i;
                        break;
                    case "TAC":
                        tacIndex = i;
                        break;
                    case "COLLECTTIME":
                    case "Collect Time":
                        line[i] = "Collect Time";
                        collectTimeIndex = i;
                        break;
                    case "MANUFACTURER":
                        manufacturerIndex = i;
                        break;
                    case "Band":
                    case "MaxULThroughput":
                    case "MaxDLThroughput":
                    case "RSRP":
                    case "RSRQ":
                    case "RSSI":
                    case "SINR":
                        numberSet.add(i);
                        break;
                }
            }

            tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(functionSubsetName.toLowerCase());
            fileHeader = "DATA_DATE" + AbsParserEngine.resultParameter
                    + "FILE_ID" + AbsParserEngine.resultParameter
                    + "ECGI_CELLID" + AbsParserEngine.resultParameter
                    + "ECGI_CELLID2" + AbsParserEngine.resultParameter
                    + "ECGI_PLMN" + AbsParserEngine.resultParameter
                    + "CELL_ID" + AbsParserEngine.resultParameter
                    + "ESN_CONST" + AbsParserEngine.resultParameter
                    + CommonLibrary.joinString(line, AbsParserEngine.resultParameter);
            isValueReadStarted = true;
            return;
        }

        if (tableObject == null) {
            return;
        }

        if (isValueReadStarted) {

            //clean data
            for (int i = 0; i < line.length; i++) {
                line[i] = line[i].replace("\t", " ").replace("\n", " ").trim();
                //String to integer
                if (numberSet.contains(i)) {
                    line[i] = line[i].replaceAll("[^0-9-.-]", "").trim();
                }
            }

            String dataDate = line[collectTimeIndex].split("\\:")[0] + ":00:00";
            line[collectTimeIndex] = line[collectTimeIndex].split("\\.")[0];

            String ecgiCellId = "";
            String ecgiCellId2 = "";
            String ecgiPlmn = "";
            String esnConst = line[esnIndex];
            String cellId = "";

//            if (line[manufacturerIndex].equals("ZTE")) {// HexDecimal numbers
//                try {
//                    cellId = Integer.toString(Integer.parseInt(line[tacIndex], 16));
//                } catch (Exception e) {
//                }
//
//                if (!line[ecgiIndex].isEmpty()) {
//                    try {
//                        ecgiPlmn = line[ecgiIndex].substring(0, 5);
//                    } catch (Exception e) {
//                    }
//                    
//                    String ecgiCellIdHex = line[ecgiIndex].replace(ecgiPlmn, "");
//                    for (int i = 0; i < 7 - ecgiCellIdHex.length(); i++) {
//                        ecgiCellIdHex = "0" + ecgiCellIdHex;
//                    }
//
//                    try {
//                        ecgiCellId = Integer.toString(Integer.parseInt(ecgiCellIdHex.substring(0, 5), 16));
//                        for (int i = 0; i < 7 - ecgiCellId.length(); i++) {
//                            ecgiCellId = "0" + ecgiCellId;
//                        }
//
//                        cellId = cellId + ecgiCellId.substring(ecgiCellId.length() - 5);
//                    } catch (Exception e) {
//                    }
//
//                    try {
//                        ecgiCellId2 = Integer.toString(Integer.parseInt(ecgiCellIdHex.substring(ecgiCellIdHex.length() - 2), 16));
//                        for (int i = 0; i < 3 - ecgiCellId2.length(); i++) {
//                            ecgiCellId2 = "0" + ecgiCellId2;
//                        }
//
//                        cellId = cellId + ecgiCellId2.substring(ecgiCellId2.length() - 2);
//                    } catch (Exception e) {
//                    }
//                }
//            } else {
            cellId = line[tacIndex];
            if (!line[ecgiIndex].isEmpty()) { //CELL ID:0060651-051 PLMN:28602
                try {
                    ecgiCellId = line[ecgiIndex].split("\\ ")[1].split("\\:")[1].split("\\-")[0];
                } catch (Exception e) {
                }
                try {
                    ecgiPlmn = line[ecgiIndex].split("\\ ")[2].split("\\:")[1];
                } catch (Exception e) {
                }
                if (ecgiCellId.length() >= 5) {
                    cellId = cellId + ecgiCellId.substring(ecgiCellId.length() - 5);
                }
            }
            if (line[ecgiIndex].contains("-")) {
                ecgiCellId2 = line[ecgiIndex].split("\\ ")[1].split("\\:")[1].split("\\-")[1];

                if (ecgiCellId2.length() >= 2) {
                    cellId = cellId + ecgiCellId2.substring(ecgiCellId2.length() - 2);
                }

            }
//            }

            String record = dataDate + AbsParserEngine.resultParameter
                    + fileId + AbsParserEngine.resultParameter
                    + ecgiCellId + AbsParserEngine.resultParameter
                    + ecgiCellId2 + AbsParserEngine.resultParameter
                    + ecgiPlmn + AbsParserEngine.resultParameter
                    + cellId + AbsParserEngine.resultParameter
                    + esnConst + AbsParserEngine.resultParameter
                    + CommonLibrary.joinString(line, AbsParserEngine.resultParameter);

            String result = CommonLibrary.get_RecordValue(fileHeader, record, tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter), "", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter);
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
