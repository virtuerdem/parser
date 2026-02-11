package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import com.ttgint.parserEngine.Northi.Vodafone.Parsers.EricssonSGSNXmlParser;
import java.io.File;
import com.ttgint.parserEngine.common.AbsParserEngine;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;

/**
 *
 * @author TTGParserTeam©
 */
@ParserSystem(systemType = "ESGSN", measType = "PM", operatorName = "VODAFONE")
public class ParserEngine_pm_Ericsson_sgsn extends AbsParserEngine {

    public ParserEngine_pm_Ericsson_sgsn(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public final void setProperties() {

    }

    @Override

    public void prepareParser() {
        ArrayList<File> fileList = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);
        ExecutorService executor = Executors.newFixedThreadPool(AbsParserEngine.numOfThreadParser);

        for (File each : fileList) {
            if (each.getName().endsWith(AbsParserEngine.integratedFileExtension) == false) {
                EricssonSGSNXmlParser a1 = new EricssonSGSNXmlParser(each, OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT);
                executor.execute(a1);
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
        dbHelper.callProceduresAfterParser("NORTHI_LOADER.LOADER_WORKS.EXECUTE_LOADER_WORKS(6)", "ESGSN Loader");
    }

    @Override
    public void objectProcedures() throws ProcedureException {
    }

}
