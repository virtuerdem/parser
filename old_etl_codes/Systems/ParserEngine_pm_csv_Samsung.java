/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.SamsungCmObject;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.SamsungPmCsvParserHandler;
import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_cm_xml_Samsung.copyNeList;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.exceptions.ParserEngineException;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.parserHandler.FileHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;

/**
 *
 * @author TTGETERZI
 */
@ParserSystem(systemType = "SAMSUNG", measType = "PM", operatorName = "VODAFONE")
public class ParserEngine_pm_csv_Samsung extends AbsParserEngine {

    public final static Map<String, String> neNameToIdList = new ConcurrentHashMap<>();
    public static List<SamsungCmObject> objectList
            = Collections.synchronizedList(new ArrayList<SamsungCmObject>());
    public final static ConcurrentHashMap<String, SamsungCmObject> neNameToObjectList2g = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String, SamsungCmObject> neNameToObjectList3g = new ConcurrentHashMap<>();
    public final static ConcurrentHashMap<String, String> generatedcnumtoCellId = new ConcurrentHashMap<>();

    public ParserEngine_pm_csv_Samsung(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    public static ArrayList<String> familiyList = new ArrayList<>();

    @Override
    public void setProperties() throws ParserEngineException {

    }

    @Override
    public void prepareParser() throws ParserEngineException {

        fetchObject();

        System.out.println("Active Ne Size : " + neNameToIdList.size());
        System.out.println("Object Size : " + objectList.size());

        ArrayList<File> fileList = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);
        ExecutorService executr = Executors.newFixedThreadPool(AbsParserEngine.numOfThreadParser);
        for (File each : fileList) {
            if (each.getName().endsWith(".csv")) {
                FileHandler handler = null;
                if (each.getName().contains("_3.1.0")) {
                    handler = new SamsungPmCsvParserHandler(each, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
                    executr.execute(handler);
                }
            }
            //  break;
        }

        executr.shutdown();
        while (!executr.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
    }

    @Override
    public void loaderProcedures() throws ProcedureException {
    }

    @Override
    public void objectProcedures() throws ProcedureException {
    }

    private void fetchObject() {
        try {
            ResultSet rss = AbsParserEngine.dbHelper.getSamsung2gObjectsList(AbsParserEngine.rawSchemaName);
            while (rss.next()) {
                String neId = rss.getString("NE_ID");
                String parentId = rss.getString("PARENT_ID");
                String topParentId = rss.getString("TOP_PARENT_ID");
                String neName = rss.getString("NE_NAmE");
                String neTypeId = rss.getString("NE_TYPE");
                SamsungCmObject object = new SamsungCmObject(neId, parentId, topParentId, neName, neTypeId);
                neNameToObjectList2g.put(neName, object);
                objectList.add(object);
            }
            rss.close();
            rss = AbsParserEngine.dbHelper.getSamsung3gObjectsList(AbsParserEngine.rawSchemaName);
            while (rss.next()) {
                String neId = rss.getString("NE_ID");
                String parentId = rss.getString("PARENT_ID");
                String topParentId = rss.getString("TOP_PARENT_ID");
                String neName = rss.getString("NE_NAmE");
                String neTypeId = rss.getString("NE_TYPE");
                SamsungCmObject object = new SamsungCmObject(neId, parentId, topParentId, neName, neTypeId);
                neNameToObjectList3g.put(neName, object);
                objectList.add(object);
            }
            rss.close();
            rss = AbsParserEngine.dbHelper.getActiveSamsungBscAndRnc();
            try {
                while (rss.next()) {
                    String neName = rss.getString("NE_NAME");
                    String neId = rss.getString("RAW_NE_ID");
                    neNameToIdList.put(neName, neId);
                    copyNeList.add(neName);
                }
            } catch (SQLException ex) {
            }

            rss.close();
        } catch (Exception ex) {
            Logger.getLogger(ParserEngine_pm_csv_Samsung.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static String findMyTopBscName(String bsName) {
        String neName = null;
        e:
        for (SamsungCmObject each : neNameToObjectList2g.values()) {
            if (each.getNeName().equals(bsName)) {

                for (SamsungCmObject eac2h : neNameToObjectList2g.values()) {
                    if (eac2h.getNeId().equals(each.getTopParentId())) {

                        neName = eac2h.getNeName();
                        break e;
                    }

                }

            }

        }
        return neName;
    }

    public static String findMyTopRncName(String bsName) {
        String neName = null;
        e:
        for (SamsungCmObject each : neNameToObjectList3g.values()) {
            if (each.getNeName().equals(bsName)) {

                for (SamsungCmObject eac2h : neNameToObjectList3g.values()) {
                    if (eac2h.getNeId().equals(each.getTopParentId())) {

                        neName = eac2h.getNeName();
                        break e;
                    }

                }

            }

        }
        return neName;
    }

    public static SamsungCmObject findMyTopBscAndReturn(String bsName) {
        SamsungCmObject neName = null;
        e:
        for (SamsungCmObject each : neNameToObjectList2g.values()) {
            if (each.getNeName().equals(bsName)) {

                for (SamsungCmObject eac2h : neNameToObjectList2g.values()) {
                    if (eac2h.getNeId().equals(each.getTopParentId())) {

                        neName = eac2h;
                        break e;
                    }

                }

            }

        }
        return neName;
    }

    public static SamsungCmObject findMyTopRncAndReturn(String bsName) {
        SamsungCmObject neName = null;
        e:
        for (SamsungCmObject each : neNameToObjectList3g.values()) {
            if (each.getNeName().equals(bsName)) {

                for (SamsungCmObject eac2h : neNameToObjectList3g.values()) {
                    if (eac2h.getNeId().equals(each.getTopParentId())) {

                        neName = eac2h;
                        break e;
                    }

                }

            }

        }
        return neName;
    }

}
