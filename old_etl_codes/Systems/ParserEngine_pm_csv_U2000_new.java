/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.HWU2000NewObjectListener;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.HWU2000NewParserHandler;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.common.ParserSystems;
import com.ttgint.parserEngine.exceptions.ParserEngineException;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;

/**
 *
 * @author TTGETERZI
 */
@ParserSystems(system = {
    @ParserSystem(systemType = "HWU2000", measType = "PM", operatorName = "VODAFONE"),
    @ParserSystem(systemType = "HWU2000", measType = "PM", operatorName = "KKTC-TELSIM")})
public class ParserEngine_pm_csv_U2000_new extends AbsParserEngine {

    private static final String OBJECT_TABLE_NAME = "U2000_OBJECTS_TEMP";
    public static HashMap<String, String> resourceNameToObjectId = new HashMap<>();
    public static HashMap<String, String> mediaCapacityInterfaceWrongStringForMail = new HashMap<>();

    public ParserEngine_pm_csv_U2000_new(ParserSystem parserSystem) throws SQLException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() throws ParserEngineException {

        try {

            try {
                ResultSet rss = AbsParserEngine.dbHelper.getU2000Objects(AbsParserEngine.rawSchemaName);
                while (rss.next()) {
                    String objectId = rss.getString(("OBJECT_ID"));
                    String resourceName = rss.getString("RESOURCE_NAME");
                    resourceNameToObjectId.put(resourceName, objectId);
                }
            } catch (Exception ex) {

            }
            System.out.println("Active Object Size :" + resourceNameToObjectId.size());
            Thread.sleep(2000);
        } catch (InterruptedException ex) {
            Logger.getLogger(ParserEngine_pm_csv_U2000_new.class.getName()).log(Level.SEVERE, null, ex);

        }
        System.out.println("Parser Started");
    }

    @Override
    public void prepareParser() throws ParserEngineException {
        ArrayList<File> fileList = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);
        ExecutorService executor = Executors.newFixedThreadPool(AbsParserEngine.numOfThreadParser);

        for (File each : fileList) {
            if (each.getName().endsWith(".csv")) {
                HWU2000NewParserHandler parserObject
                        = new HWU2000NewParserHandler(each,
                                OperationSystemEnum.UNIX, ProgressTypeEnum.PRODUCT);
                executor.execute(parserObject);
            }
        }

        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }

        if (HWU2000NewObjectListener.objectList.isEmpty() == false) {
            File objectFile = new File((AbsParserEngine.LOCALFILEPATH + OBJECT_TABLE_NAME + integratedFileExtension));
            FileOutputStream out;
            try {
                out = new FileOutputStream(objectFile);
                for (HWU2000NewObjectListener.U2000Object each : HWU2000NewObjectListener.objectList.values()) {
                    String line = each.toString();

                    out.write((line + "\n").getBytes());

                }
                out.close();

            } catch (FileNotFoundException ex) {

            } catch (IOException ex) {

            }
        }

        //Musteriye yanlis || || resourceName bildirme
        if (AbsParserEngine.operatorName.equals("VODAFONE")) {
            if (CommonLibrary.get_CurrentDatetime("HH").equals("10") && !mediaCapacityInterfaceWrongStringForMail.isEmpty()) {
                try {
                    dbHelper.truncateRawTable("U2000_NEW", "INCORRECT_RESOURCES");
                } catch (Exception ex) {
                }
                //Bölünyor tek maille gitmedigi icin
                for (String resource : mediaCapacityInterfaceWrongStringForMail.keySet()) {
                    try {
                        dbHelper.insertIncorrectResources(mediaCapacityInterfaceWrongStringForMail.get(resource), resource);
                    } catch (Exception ex) {
                    }
                }
            }
        }

        if (!AbsParserEngine.operatorName.equals("VODAFONE") && CommonLibrary.get_CurrentDatetime("HH").equals("10")) {
            try {
                dbHelper.truncateRawTable("U2000_NEW", "INCORRECT_RESOURCES_ALLDAY");
            } catch (Exception ex) {
            }
        }
        if (!mediaCapacityInterfaceWrongStringForMail.isEmpty()) {
            for (String resource : mediaCapacityInterfaceWrongStringForMail.keySet()) {
                try {
                    dbHelper.insertIncorrectResourcesAllDay(mediaCapacityInterfaceWrongStringForMail.get(resource), resource, AbsParserEngine.operatorName);
                } catch (Exception ex) {
                }
            }
        }
    }

    @Override
    public void loaderProcedures() throws ProcedureException {
        switch (AbsParserEngine.operatorName) {
            case "VODAFONE":
                dbHelper.callProceduresAfterParser("U2000_NEW.LOADER_WORKS_U2000.EXECUTE_LOADER_WORKS_U2000", "HW U2000 Loader");
                break;
            case "KKTC-TELSIM":
                dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(18)", "HW U2000 Loader");
                break;
        }

    }

    @Override
    public void objectProcedures() throws ProcedureException {
        switch (AbsParserEngine.operatorName) {
            case "VODAFONE":
                dbHelper.callProceduresAfterParser("U2000_NEW.U2000_OBJECTS_PROC", "HW U2000 Update Objects");
                break;
            case "KKTC-TELSIM":
                dbHelper.callProceduresAfterParser("NORTHI_PARSER_KKTC.U2000_OBJECTS_PROC", "HW U2000 Update Objects");
                break;
        }

    }

}
