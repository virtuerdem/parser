/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.parserHandler.FileReaderHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;

/**
 *
 * @author erdigurbuz
 */
public class EricssonSGSNLogTxtParser extends FileReaderHandler {

    private final RawTableObject tableObject = TableWatcher.getInstance().getTableObjectFromTableName("LOG_SGSN");
    private final String tableColumnNames = tableObject.getFullColumnOrderUsingCounterNameFil("|");
    private final String sgsnName;
    private String fullPath;
    private String fileHeaderNames = "";
    private String record = "";
    private Date fileContentDataDate;
    private Boolean eventStartingFlag = false;
    private Boolean dateSetFlag = false;

    public EricssonSGSNLogTxtParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType, String sgsnName) {
        super(currentFileProgress, operationSystem, progType);
        this.sgsnName = sgsnName;
    }

    @Override
    public void onStartParseOperation() {
    }

    @Override
    public void lineProgress(String line) {
        try {
            if (!dateSetFlag) {
                if (line.contains("date=")) {
                    fileContentDataDate = new SimpleDateFormat("yyyy-MM-dd").parse(getValueFromKey(line));
                } else if (line.contains("time=")) {
                    fileContentDataDate = new SimpleDateFormat("yyyy-MM-dd HH").parse(new SimpleDateFormat("yyyy-MM-dd").format(fileContentDataDate) + " " + getValueFromKey(line).split(":")[0]);
                    dateSetFlag = true;
                    fileHeaderNames = "DATA_DATE" + AbsParserEngine.resultParameter;
                    record = new SimpleDateFormat(CommonLibrary.getResultFileDateFormat()).format(fileContentDataDate) + AbsParserEngine.resultParameter;
                    fullPath = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + "-" + new SimpleDateFormat(CommonLibrary.getResultFileDateFormat()).format(fileContentDataDate) + AbsParserEngine.integratedFileExtension;
                }
                return;
            }

            if (!eventStartingFlag && line.contains("==EVENT==")) {
                eventStartingFlag = true;
                fileHeaderNames += "SGSN_NAME" + AbsParserEngine.resultParameter;
                record += sgsnName + AbsParserEngine.resultParameter;
                return;
            }

            if (eventStartingFlag) {
                for (String counterNameFile : tableColumnNames.split("\\|")) {
                    if (line.split("=")[0].trim().equals(counterNameFile)) {
                        fileHeaderNames += counterNameFile + AbsParserEngine.resultParameter;
                        if (counterNameFile.equals("imsi")) {
                            if (CommonLibrary.isNumeric(getValueFromKey(line))) {
                                record += getValueFromKey(line).substring(0, 5) + AbsParserEngine.resultParameter;
                            } else {
                                record += "0" + AbsParserEngine.resultParameter;
                            }
                        } else {
                            record += getValueFromKey(line) + AbsParserEngine.resultParameter;
                        }
                        break;
                    }
                }
            }

            if (fileHeaderNames.split(AbsParserEngine.resultParameter).length - 1 == tableColumnNames.split("|").length) {
                eventStartingFlag = false;

                writeIntoFilesWithController(fullPath, CommonLibrary.get_RecordValue(fileHeaderNames, record, tableColumnNames, "0", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter) + "\n");
                fileHeaderNames = fileHeaderNames.split("\\|")[0] + AbsParserEngine.resultParameter;
                record = record.split("\\|")[0] + AbsParserEngine.resultParameter;
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String getValueFromKey(String line) {
        return line.split("=")[1].trim();
    }

    @Override
    public void onstopParseOperation() {
    }
}
