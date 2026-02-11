/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_csv_HW_3g;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.vendorID;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.parserHandler.CsvFileHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

/**
 *
 * @author ibrahimegerci
 */
public class HW3GPmCsvParser3GNew extends CsvFileHandler implements Runnable {

    private RawTableObject tableObject;
    private String tableColumnNames;
    private int lineCount = 0;
    private String fileName;
    private String fileDate;
    private String fullPath;
    private String fileHeaderNames;
    private int notFoundNodebCount = 0;

    public HW3GPmCsvParser3GNew(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
        fileName = currentFileProgress.getName();
    }

    @Override
    public void onStartParseOperation() {
        tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetId(currentFileProgress.getName().split("\\_")[2]);
        tableColumnNames = tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter);
        fullPath = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;
        fileDate = currentFileProgress.getName().split("\\_")[3];
    }

    @Override
    public void lineProgress(String[] line) {
        lineCount++;

        if (lineCount == 1) {
            fileHeaderNames = "DATA_DATE" + AbsParserEngine.resultParameter
                    + "GRANULARITY_PERIOD" + AbsParserEngine.resultParameter
                    + "NETWORK_ID" + AbsParserEngine.resultParameter
                    + "LOCAL_CELL_ID" + AbsParserEngine.resultParameter
                    + CommonLibrary.joinString(line, AbsParserEngine.resultParameter);
            return;
        }

        if (lineCount > 2) {
            try {
                String dataDate = line[0].replace(":", "").replace(" ", "").replace("-", "");
                String nodebName = line[2].split("/")[0];

                if (ParserEngine_pm_csv_HW_3g.nodebNameAndIDList.containsKey(nodebName)) {
                    String nodebId = ParserEngine_pm_csv_HW_3g.nodebNameAndIDList.get(nodebName).split("\\|")[0];
                    String localCellId = "";

                    if (tableObject.getTableType().equals("LOCALCELL")) {
                        String rncID = ParserEngine_pm_csv_HW_3g.nodebNameAndIDList.get(nodebName).split("\\|")[1];
                        String rncRawId = ParserEngine_pm_csv_HW_3g.rncNameAndIDList.get(rncID);
                        String localCellRawId = line[2].split(", Local Cell ID=")[1].split(",")[0];
                        localCellId = networkIdGenerator(Integer.parseInt(rncRawId), Integer.parseInt(localCellRawId), "LOCALCELL");
                    }

                    String record = dataDate + AbsParserEngine.resultParameter
                            + line[1] + AbsParserEngine.resultParameter
                            + nodebId + AbsParserEngine.resultParameter
                            + localCellId + AbsParserEngine.resultParameter
                            + CommonLibrary.joinString(line, AbsParserEngine.resultParameter);

                    for (int i = 0; i < fileHeaderNames.split("\\" + AbsParserEngine.resultParameter).length - record.split("\\" + AbsParserEngine.resultParameter).length; i++) {
                        record = record + AbsParserEngine.resultParameter;
                    }

                    writeIntoFilesWithController(fullPath,
                            CommonLibrary.get_RecordValue(fileHeaderNames, record, tableColumnNames, "0",
                                    AbsParserEngine.resultParameter, AbsParserEngine.resultParameter) + "\n");
                } else {
                    logNotFoundNodebNames(nodebName, fileDate);
                }
            } catch (Exception ex) {
                System.err.println(fileName + "\n"
                        + " Writer - Error Line:" + lineCount + "\n"
                        + " FileHeader: " + fileHeaderNames + "\n"
                        + " Record    : " + CommonLibrary.joinString(line, AbsParserEngine.resultParameter) + "\n"
                        + ex.getMessage()
                );
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onstopParseOperation() {
        if (notFoundNodebCount > 0) {
            System.err.println("** notFoundNodebCount : " + notFoundNodebCount + " at " + currentFileProgress.getName());
        }
    }

    private String networkIdGenerator(int parentID, int childID, String neType) {
        BigDecimal bd = new BigDecimal("10");
        BigDecimal resultBigDecimal = bd.pow(21).multiply(BigDecimal.valueOf(vendorID)).
                add(bd.pow(16).multiply(BigDecimal.valueOf(ParserEngine_pm_csv_HW_3g.CELLCLASSTYPEID))).
                add(bd.pow(8).multiply(BigDecimal.valueOf(parentID))).
                add(BigDecimal.valueOf(childID));

        return resultBigDecimal.toString();
    }

    private void logNotFoundNodebNames(String nodebName, String dataDate) throws FileNotFoundException, IOException {
        notFoundNodebCount++;
        FileOutputStream out = new FileOutputStream(AbsParserEngine.LOCALFILEPATH + "notFoundNodebNames.dat", true);
        out.write((dataDate + "|" + nodebName + "\n").getBytes());
        out.close();
    }

}
