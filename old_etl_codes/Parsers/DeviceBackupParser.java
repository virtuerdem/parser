/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import jxl.Cell;
import jxl.CellType;
import jxl.Sheet;
import com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_Storage_Backup;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.exceptions.DBSessionException;
import com.ttgint.parserEngine.parserHandler.XlsFileHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;

/**
 *
 * @author TTGETERZI
 */
public class DeviceBackupParser extends XlsFileHandler {

    public DeviceBackupParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    private String dateDate;
    private String dateFromFile;
    private Date dateObject;
    private final SimpleDateFormat dateFormatterFromFile = new SimpleDateFormat("yyyyMMdd");

    @Override
    public void onStartParseOperation() {
        String fileName = currentFileProgress.getName();

        dateFromFile = fileName.split("\\_")[1].replace("-", "").trim();

        try {
            dateObject = dateFormatterFromFile.parse(dateFromFile);

            Calendar cal = Calendar.getInstance();
            cal.setTime(dateObject);
            cal.add(Calendar.DATE, -1);
            dateObject = cal.getTime();
        } catch (ParseException ex) {
            //  ex.printStackTrace();
        }
        dateDate = new SimpleDateFormat("yyyyMMdd").format(new Date());
    }

    @Override
    public void parseSheets(Sheet[] sheets) {

        for (Sheet eachSheet : sheets) {
            Cell cell = eachSheet.getCell(0, 0);
            String[] splittedHeader = cell.getContents().split("\\/");
            String DEVICE_TYPE = splittedHeader[splittedHeader.length - 1];
            String query = null;
            for (int i = 5; i < eachSheet.getRows(); i++) {
                Cell DEVICE_NAME = eachSheet.getCell(0, i);
                Cell AVAILABILITY = eachSheet.getCell(1, i);
                if (DEVICE_NAME.getType().equals(CellType.EMPTY)) {
                    continue;
                }
                String deviceName = DEVICE_NAME.getContents();
                String availability;
                if (AVAILABILITY.getType().equals(CellType.EMPTY)) {

                    availability = "-1";
                } else {
                    availability = AVAILABILITY.getContents();
                }
                if (availability.equals("")) {
                    continue;
                    //  availability = "-1";
                }

                query = "insert into " + AbsParserEngine.rawSchemaName + "." + ParserEngine_Storage_Backup.systemTableName + "(DEVICE_TYPE,DEVICE_NAME,AVAILABILITY,DAY_ID) "
                        + " select '" + DEVICE_TYPE + "','" + deviceName + "'," + availability + ",to_date('" + dateFormatterFromFile.format(dateObject)
                        + "','YYYYMMDD') FROM DUAL";
                
                //System.out.println(query);
                try {
                    AbsParserEngine.dbHelper.executeQueryDirect(query);
                } catch (DBSessionException ex) {

                }

            }

            //  break;
        }
    }

    @Override
    public void onstopParseOperation() {

    }

}
