/*
 * To change this license header, choose License Headers in Project Properties.j
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.FemtoXmlPmHandler;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.LOCALFILEPATH;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import static com.ttgint.parserEngine.common.AbsParserEngine.numOfThreadParser;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author erdigurbuz
 */
@ParserSystem(systemType = "FEMTO", measType = "PM", operatorName = "VODAFONE")
public class ParserEngine_pm_FemtoBSR extends AbsParserEngine {

    public List<RawTableObject> allActiveTableNames = TableWatcher.getInstance().getCopyOfSystemTableList();
    public static ConcurrentHashMap<String, String> objects = new ConcurrentHashMap<>();
    public static ConcurrentHashMap<String, String> parentMap = new ConcurrentHashMap<>();

    public ParserEngine_pm_FemtoBSR(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() {
    }

    @Override
    public void prepareParser() {
        try {
            ExecutorService executorForXmlParser = Executors.newFixedThreadPool(numOfThreadParser);
            List<File> fileList = CommonLibrary.list_AllFilesAsFile(LOCALFILEPATH);
            for (File xmlFile : fileList) {

                Runnable xmlParseThread = null;
                if (xmlFile.getName().contains("_SubNetwork")) {
                    try {
                        xmlParseThread = new FemtoXmlPmHandler(xmlFile, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
                    } catch (Exception ex) {
                        dbHelper.insertParserException(ex);
                    }
                    executorForXmlParser.execute(xmlParseThread);
                }
            }

            executorForXmlParser.shutdown();
            while (!executorForXmlParser.isTerminated()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                }
            }

            //Object raw table
            if (objects.size() > 0) {

                File objFile = new File(AbsParserEngine.LOCALFILEPATH + "OBJECTS_FEMTO" + AbsParserEngine.integratedFileExtension);

                FileOutputStream objectFile = new FileOutputStream(objFile);
                for (String neId : objects.keySet()) {
                    String record = objects.get(neId);
                    objectFile.write(record.getBytes());
                }
                String topParentRow = parentMap.get("topParentId");
                String parentRow = parentMap.get("parentId");
                objectFile.write((topParentRow + "\n" + parentRow).getBytes());
                objectFile.close();
            }

            System.out.println("All pm xmls parsed");

        } catch (Exception ex) {
            dbHelper.insertParserException(ex);
        }

    }

    @Override
    public void loaderProcedures() throws ProcedureException {
        dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(19)", "Femto Loader");
    }

    @Override
    public void objectProcedures() throws ProcedureException {

        //Procedure'ler calistiriliyor (4 saate bir)
        Date currentDate = new Date();
        int hour = Integer.parseInt(new SimpleDateFormat("HH").format(currentDate));
        if (hour % 4 == 0) {
            dbHelper.callProceduresAfterParser("NORTHI_PARSER.OBJECTS_PROCESS_ALU_FEMTO", "ALU FEMTO Object");
            dbHelper.callProceduresAfterParser("NORTHI_PARSER_SETTINGS.OBJECTS_FEMTO_ELEMT_UPDATE", "Femto Element update Procedure");
        }
    }

}
