package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import java.io.File;
import com.ttgint.parserEngine.common.AbsParserEngine;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.EricssonESPPXmlParser;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;

/**
 *
 * @author TTGParserTeam©
 */
@ParserSystem(systemType = "ESPP", measType = "PM", operatorName = "VODAFONE")
public class ParserEngine_pm_Ericsson_spp extends AbsParserEngine {

    public static ArrayList<String> sppRawTableNames = new ArrayList<>();

    public ParserEngine_pm_Ericsson_spp(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public final void setProperties() {
    }

    @Override
    public void prepareParser() {
        try {
            ResultSet rss = AbsParserEngine.dbHelper.getEricssonSPPrawTableNames();
            while (rss.next()) {
                sppRawTableNames.add(rss.getString("TABLE_NAME"));
            }
        } catch (Exception ex) {
            Logger.getLogger(ParserEngine_pm_Ericsson_spp.class.getName()).log(Level.SEVERE, null, ex);
        }
        //  System.out.println(sppRawTableNames);

        File[] fileList = new File(AbsParserEngine.LOCALFILEPATH).listFiles();
        ExecutorService executor = Executors.newFixedThreadPool(AbsParserEngine.numOfThreadParser);
        for (File eachFile : fileList) {
            if (eachFile.getName().endsWith(".xml")) {
                // System.out.println("girdi");
                executor.execute(new EricssonESPPXmlParser(eachFile, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT));
            }
        }
        executor.shutdown();
        while (!executor.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
    }

    @Override
    public void loaderProcedures() throws ProcedureException {
        dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS_ESPP", "ESPP Loader");
    }

    @Override
    public void objectProcedures() throws ProcedureException {
    }
}
