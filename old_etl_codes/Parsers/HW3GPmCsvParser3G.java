package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_csv_HW_3g;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.vendorID;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import static com.ttgint.parserEngine.commonLibrary.CommonLibrary.replaceNullValuesWithZero;
import com.ttgint.parserEngine.exceptions.ParserIOException;
import com.ttgint.parserEngine.parserHandler.FileReaderHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;

public class HW3GPmCsvParser3G extends FileReaderHandler implements Runnable {

    private String unlFile;
    private String dataType;
    private String dataDate;
    private String rawTableName;
    private String functionSubSetID;
    private String fileHeaderNames = "DATA_DATE|GRANULARITY_PERIOD|NETWORK_ID|";
    private String tableColumnNames;
    private String iP;
    private RawTableObject tableObject;

    public HW3GPmCsvParser3G(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void lineProgress(String line) {
        parseCsvFile(line);
        index++;
    }

    @Override
    public void onStartParseOperation() {

        dataDate = currentFileProgress.getName().split("\\_")[3];
        iP = currentFileProgress.getName().split("\\-")[0];
        functionSubSetID = currentFileProgress.getName().split("\\_")[2];
        tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetId(functionSubSetID);
        rawTableName = tableObject.getTableName();
        unlFile = AbsParserEngine.LOCALFILEPATH + rawTableName + AbsParserEngine.integratedFileExtension;
        if (rawTableName.contains("LOCAL_CELL")) {
            dataType = "LOCAL_CELL";
            fileHeaderNames += "LOCAL_CELL_ID|RELIABILITY";
        } else {
            dataType = "NODEB";
            fileHeaderNames += "RELIABILITY";
        }
    }

    @Override
    public void onstopParseOperation() {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

//    public void setTableColumnNames(String tableColumnNames) throws IOException {
//        this.tableColumnNames = northi.commonLibrary.CommonLibrary.get_NorthiTableColumnsForGetValue(rawTableName, "C");
//    }
    int index = 0;

    private void parseCsvFile(String line) {
        String record = line;
        try {
            if (index == 0) {
                fileHeaderNames += record.split("Reliability")[1].replace(",", "|").replace("\"", "");
                return;
            }
            if (index > 1) {

                switch (dataType) {
                    case "LOCAL_CELL":
                        String dataDateAndPeriod = record.split("\"")[0].replace(":", "").replace(" ", "").replace("-", "");
                        String nodebNameAndLocalCellID = record.split("\"")[1];
                        String nodebName = null;

                        String localCellRawID = null;
                        nodebName = nodebNameAndLocalCellID.split("/ULoCell:NodeB ")[0];
                        localCellRawID = nodebNameAndLocalCellID.split(", Local Cell ID=")[1].split(",")[0];

                        String counterValues = record.split("\"")[2];

                        if (ParserEngine_pm_csv_HW_3g.nodebNameAndIDList.containsKey(nodebName)) {
                            String nodebID = ParserEngine_pm_csv_HW_3g.nodebNameAndIDList.get(nodebName).split("\\|")[0];
                            String rncID = ParserEngine_pm_csv_HW_3g.nodebNameAndIDList.get(nodebName).split("\\|")[1];
                            String rncRawID = ParserEngine_pm_csv_HW_3g.rncNameAndIDList.get(rncID);
                            String localCellID = networkIdGenerator(Integer.parseInt(rncRawID), Integer.parseInt(localCellRawID), "LOCALCELL");
                            String updatedRecord = dataDateAndPeriod.replace(",", "|") + nodebID + "|" + localCellID + counterValues.replace(",", "|");
                            updatedRecord = replaceNullValuesWithZero(updatedRecord);
                            tableColumnNames = tableObject.getFullColumnOrderUsingCounterNameFil("|");
                            updatedRecord = com.ttgint.parserEngine.commonLibrary.CommonLibrary.get_RecordValue(fileHeaderNames, updatedRecord, tableColumnNames, "0", "|", "|") + "\n";
                            writeIntoFilesWithController(unlFile, updatedRecord);
                        } else {
                            logNotFoundNodebNames(nodebName, dataDate);
                        }
                        break;
                    case "NODEB":
                        dataDateAndPeriod = record.split("\"")[0].replace(":", "").replace(" ", "").replace("-", "");
                        nodebName = record.split("\"")[1].split("/NodeBFunction:")[0];
                        counterValues = record.split("\"")[2];

                        if (ParserEngine_pm_csv_HW_3g.nodebNameAndIDList.containsKey(nodebName)) {
                            String nodebID = ParserEngine_pm_csv_HW_3g.nodebNameAndIDList.get(nodebName).split("\\|")[0];

                            String updatedRecord = dataDateAndPeriod.replace(",", "|") + nodebID + counterValues.replace(",", "|");
                            updatedRecord = replaceNullValuesWithZero(updatedRecord);
                            tableColumnNames = tableObject.getFullColumnOrderUsingCounterNameFil("|");
                            updatedRecord = com.ttgint.parserEngine.commonLibrary.CommonLibrary.get_RecordValue(fileHeaderNames, updatedRecord, tableColumnNames, "0", "|", "|") + "\n";
                            writeIntoFilesWithController(unlFile, updatedRecord);
                        } else {
                            logNotFoundNodebNames(nodebName, dataDate);
                        }
                        break;
                }
            }
        } catch (NumberFormatException | ParserIOException | IOException e) {

        }

    }

    private String networkIdGenerator(int parentID, int childID, String neType) {

        String result = null;
        BigDecimal bd = new BigDecimal("10");
        BigDecimal resultBigDecimal = bd.pow(21).multiply(BigDecimal.valueOf(vendorID)).
                add(bd.pow(16).multiply(BigDecimal.valueOf(ParserEngine_pm_csv_HW_3g.CELLCLASSTYPEID))).
                add(bd.pow(8).multiply(BigDecimal.valueOf(parentID))).
                add(BigDecimal.valueOf(childID));

        return resultBigDecimal.toString();
    }

    private void logNotFoundNodebNames(String nodebName, String dataDate) throws FileNotFoundException, IOException {
        FileOutputStream out = new FileOutputStream(AbsParserEngine.LOCALFILEPATH + "notFoundNodebNames.dat", true);
        out.write((dataDate + "|" + nodebName + "\n").getBytes());
        out.close();
    }

}
