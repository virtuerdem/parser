package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.EricssonPmXmlEIPWORKSParser;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.exceptions.ProcedureException;

/**
 *
 * @author TTGParserTeam©
 */
@ParserSystem(systemType = "EIPWORKS", measType = "PM", operatorName = "VODAFONE")
public class ParserEngine_pm_Ericsson_ipworks extends AbsParserEngine {

    public final static List<String> EIpWorksRawTableList = new ArrayList<>();

    public ParserEngine_pm_Ericsson_ipworks(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public void setProperties() {
    }

    @Override
    public void prepareParser() {
        try {
            ResultSet rss = AbsParserEngine.dbHelper.getEIPWorksRawTableNames();
            while (rss.next()) {
                EIpWorksRawTableList.add(rss.getString("TABLE_NAME"));
            }
        } catch (Exception ex) {
            Logger.getLogger(EricssonPmXmlEIPWORKSParser.class.getName()).log(Level.SEVERE, null, ex);
        }

        ExecutorService executorForXmlParser = Executors.newFixedThreadPool(numOfThreadParser);
        System.out.println("LOCALFILEPATH" + LOCALFILEPATH);
        ArrayList<String> fileList = CommonLibrary.list_AllFilesAsString(LOCALFILEPATH);

        for (String csvFile : fileList) {

            if (csvFile.endsWith(".xml")) {
                Runnable csvParseThread = null;
                csvParseThread = (Runnable) new EricssonPmXmlEIPWORKSParser(csvFile, EIpWorksRawTableList.get(0));
                executorForXmlParser.execute(csvParseThread);
            }
        }

        executorForXmlParser.shutdown();
        while (!executorForXmlParser.isTerminated()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
            }
        }
        System.out.println("All pm csv parsed");
    }

    @Override
    public void loaderProcedures() throws ProcedureException {
        dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS_EIPWORKS", "EIPWORKS LOADER");
    }

    @Override
    public void objectProcedures() throws ProcedureException {
    }

}
