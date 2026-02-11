/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_WDM;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ParserIOException;
import com.ttgint.parserEngine.parserHandler.CsvFileHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author turgut.simsek
 */
public class WdmCsvParser extends CsvFileHandler {

    //2015-01-07 05:30:00
    private static final int deviceIdOrder = 0;
    private static final int deviceNameOrder = 1;
    private static final int resourceNameOrder = 2;
    private static final int collectionTimeOrder = 3;
    private SimpleDateFormat dateFormatterFromFile = new SimpleDateFormat("yyyyMMddHHmmss");
    boolean flag = true;

    private boolean isValuesStarted = false;
    private int headerSize = -1;
    private boolean lineCheck = false;
    private String header;
    private Date dateObject;
    private String collectionTime = "";
    private String functionSubsetId;
    private RawTableObject tableObject;
    private String neName;
    private String shelfName;
    private String slotId;
    private String boardNAME;
    private String portId;
    private String line;
    private String resourceName;
    private String objectId;

    public WdmCsvParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void onStartParseOperation() {
        String fileName = currentFileProgress.getName();
        functionSubsetId = fileName.split("\\_")[1];
        tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetId(functionSubsetId);

    }

    @Override
    public void lineProgress(String[] splitted) {

        if (tableObject == null) {
            return;
        }

        if (isValuesStarted) {
            try {
                collectionTime = splitted[collectionTimeOrder];
                collectionTime = collectionTime.replace("-", "");
                collectionTime = collectionTime.replace(":", "");
                collectionTime = collectionTime.replace(" ", "");

                dateObject = dateFormatterFromFile.parse(collectionTime);
                collectionTime = dateFormatterFromFile.format(dateObject);
                splitted[collectionTimeOrder] = collectionTime;
            } catch (ParseException ex) {
                System.out.println(collectionTime + "   -> " + ex);
                return;
            }
        }

        line = CommonLibrary.joinString(splitted, AbsParserEngine.resultParameter);

        if (line.startsWith("DeviceID")) {
            header = line;
            isValuesStarted = true;
            return;
        }

        lineCheck = false;

        if (isValuesStarted) {

            line = fixLine(line);

            addConstantCounterNameValues();

            WdmObjectListener.addObject(splitted[deviceIdOrder], neName, resourceName, neName, shelfName, slotId, boardNAME, portId);

            if (objectId != null) {
                line = line + AbsParserEngine.resultParameter + objectId;
                String resut = CommonLibrary.get_RecordValue(header, line, tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter), "0", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter);
                resut += "\n";
                String fileName = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;

                try {
                    writeIntoFilesWithController(fileName, resut);
                } catch (ParserIOException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void addConstantCounterNameValues() {

        String rp = AbsParserEngine.resultParameter;

        String[] lineArr = line.split("\\" + rp);

        resourceName = lineArr[resourceNameOrder];
        resourceName = resourceName.trim();
        objectId = ParserEngine_pm_WDM.resourceNameToObjectId.get(resourceName);

        if (!header.contains("OBJECT_ID")) {
            header = "OBJECT_ID" + rp + "NE_NAME" + rp + "SHELF_NAME" + rp + "SLOT_ID" + rp + "BOARD_NAME" + rp + "PORT_ID" + rp + header;
        }
        //   
        parseResourceName(resourceName);

        line = objectId + rp + neName + rp + shelfName + rp + slotId + rp + boardNAME + rp + portId +rp+ line;

    }

    @Override
    public void onstopParseOperation() {
        if (lineCheck) {
            System.out.println("Corrupted " + currentFileProgress.getName());
        }
        currentFileProgress.delete();
    }

    public void parseResourceName(String resourceName) {

        String[] resourceArr = resourceName.split("\\-");

        neName = resourceArr[0];
        shelfName = resourceArr[1];
        slotId = resourceArr[2];
        boardNAME = resourceArr[3];

        String regex = "\\d+";
        Pattern pattern = Pattern.compile(regex, Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(resourceArr[4]);
        if (matcher.find()) {
            portId = matcher.group(0);
        } else {
            portId = "0";
        }

    }

    private String fixLine(String line) {
        line = discard(line);
        if (line.endsWith(",")) {
            line += 0;
        }
        line = line.replace("\"", "");
        line = line.replace("'", "");
        line = line.replace("?", "");

        return line;
    }

    private String discard(String line) {
        StringBuilder st = new StringBuilder();
        char[] charlist = line.toCharArray();
        for (char each : charlist) {
            if (Character.UnicodeBlock.of(each)
                    == Character.UnicodeBlock.BASIC_LATIN) {
                st.append(each);
            }
        }
        return st.toString();
    }

}
