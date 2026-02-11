package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.parserHandler.FileReaderHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 * @author ibrahimegerci
 */
public class DefneCliPmTxtFileHandler extends FileReaderHandler {

    private RawTableObject tableObject;
    private String tableColumnNames = "";
    private String outputFileName = "";
    private String fileContentDataDate = "";
    private String fileContentDataSource = "";
    private String fileHeaderNames = "";
    private int rowCount = 0;
    private final String concat = AbsParserEngine.resultParameter;

    public DefneCliPmTxtFileHandler(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void onStartParseOperation() {
        tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(currentFileProgress.getName().split("\\+")[1]);
        tableColumnNames = tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter);
        outputFileName = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;
        fileContentDataSource = currentFileProgress.getName().split("\\+")[0];
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new SimpleDateFormat("yyyy-MM-dd-HH")
                    .parse(currentFileProgress.getName().split("\\+")[2].split("\\.")[2].substring(0, 13)));
            if (currentFileProgress.getName().endsWith("-12-1.csv")) {
                calendar.add(Calendar.HOUR_OF_DAY, -12);
            } else if (!currentFileProgress.getName().endsWith("-12-2.csv")
                    && currentFileProgress.getName().endsWith("-2.csv")) {
                calendar.add(Calendar.HOUR_OF_DAY, 12);
            }
            fileContentDataDate = new SimpleDateFormat("yyyyMMddHHmmss").format(calendar.getTime());
        } catch (ParseException e) {
        }

    }

    @Override
    public void lineProgress(String line) {
        rowCount++;
        try {
            StringBuilder record = new StringBuilder();
            if (rowCount == 1 && tableObject != null) {//header
                fileHeaderNames = "DATA_DATE" + concat
                        + "DATA_SOURCE" + concat
                        + line.replace(",", concat);
            } else if (rowCount > 1 && tableObject != null && !line.contains("HOSTNAME,")) { //values
                record.append(fileContentDataDate).append(concat)
                        .append(fileContentDataSource).append(concat)
                        .append(line.replace(",", concat));

                //prepare record
                for (int i = 0; i < fileHeaderNames.split("\\" + concat).length - record.toString().split("\\" + concat).length; i++) {
                    record.append(concat);
                }

                String preparedRecord = CommonLibrary.get_RecordValue(
                        fileHeaderNames,
                        record.toString(),
                        tableColumnNames,
                        "0",
                        concat,
                        concat);

                if (preparedRecord
                        .replace(fileContentDataDate, "")
                        .replace(fileContentDataSource, "")
                        .replace(concat, "")
                        .replace("0", "").length() > 0) {
                    writeIntoFilesWithController(outputFileName, preparedRecord + "\n");
                }
            }

        } catch (Exception e) {
            System.err.println("*Parse Error " + e.getMessage() + " for " + currentFileProgress.getName() + " at " + rowCount + " value: " + line);
        }
    }

    @Override
    public void onstopParseOperation() {
    }

}
