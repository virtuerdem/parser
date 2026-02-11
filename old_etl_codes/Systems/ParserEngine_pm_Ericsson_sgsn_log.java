package com.ttgint.parserEngine.Northi.Vodafone.Systems;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.ResultSet;
import com.ttgint.parserEngine.common.AbsParserEngine;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.DecodeSgsnLogFile;
import com.ttgint.parserEngine.Northi.Vodafone.Parsers.EricssonSGSNLogTxtParser;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import static com.ttgint.parserEngine.common.AbsParserEngine.systemType;
import com.ttgint.parserEngine.common.ParserSystem;
import com.ttgint.parserEngine.exceptions.ProcedureException;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;

/**
 *
 * @author TTGParserTeam©
 */
@ParserSystem(systemType = "ESGSN-LOG", measType = "PM", operatorName = "VODAFONE")
public class ParserEngine_pm_Ericsson_sgsn_log extends AbsParserEngine {

    public static HashMap<String, String> neNameAndIpList = new HashMap<>();

    public ParserEngine_pm_Ericsson_sgsn_log(ParserSystem parserSystem) throws SQLException, InterruptedException, Exception {
        super(parserSystem);
    }

    @Override
    public final void setProperties() {
    }

    @Override
    public void prepareParser() {
        try {
            ResultSet rSet = dbHelper.getNEIDandNameInfo(systemType);
            while (rSet.next()) {
                neNameAndIpList.put(rSet.getString("m2000_server_ip"), rSet.getString("ne_name"));
            }
            rSet.close();
        } catch (Exception ex) {
        }

        ArrayList<File> fileList = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);
        ExecutorService executor = Executors.newFixedThreadPool(AbsParserEngine.numOfThreadParser);
        for (File each : fileList) {
            if (each.getName().startsWith("A") && each.getName().contains("_ebs") && !each.getName().endsWith(AbsParserEngine.integratedFileExtension)) {
                File sourceFile = new File(AbsParserEngine.LOCALFILEPATH + "ebm.xml");
                File targetFile = new File(AbsParserEngine.LOCALFILEPATH + each.getName().split("/")[each.getName().split("/").length - 1] + "/" + "ebm.xml");
                try {
                    Files.copy(sourceFile.toPath(), targetFile.toPath());
                } catch (IOException ex) {
                }
                sourceFile = new File(AbsParserEngine.LOCALFILEPATH + "ebm_cause_codes.xml");
                targetFile = new File(AbsParserEngine.LOCALFILEPATH + each.getName().split("/")[each.getName().split("/").length - 1] + "/" + "ebm_cause_codes.xml");
                try {
                    Files.copy(sourceFile.toPath(), targetFile.toPath());
                } catch (IOException ex) {
                }
                sourceFile = new File(AbsParserEngine.LOCALFILEPATH + "parse_ebm_log.pl");
                targetFile = new File(AbsParserEngine.LOCALFILEPATH + each.getName().split("/")[each.getName().split("/").length - 1] + "/" + "parse_ebm_log.pl");
                try {
                    Files.copy(sourceFile.toPath(), targetFile.toPath());
                } catch (IOException ex) {
                }
                DecodeSgsnLogFile a1 = new DecodeSgsnLogFile(each);
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

        //Parse
        fileList = CommonLibrary.list_AllFilesAsFile(AbsParserEngine.LOCALFILEPATH);
        executor = Executors.newFixedThreadPool(AbsParserEngine.numOfThreadParser);
        for (File each : fileList) {
            if (each.getName().startsWith("A") && each.getName().contains("_ebs") && each.getName().endsWith(AbsParserEngine.integratedFileExtension)) {
                String ipAddress = each.getName().split("\\+")[each.getName().split("\\+").length - 1].replace(AbsParserEngine.integratedFileExtension, "");
                EricssonSGSNLogTxtParser a1 = new EricssonSGSNLogTxtParser(each, OperationSystemEnum.REDHAT, ProgressTypeEnum.PRODUCT, neNameAndIpList.get(ipAddress));
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
    }

    @Override
    public void objectProcedures() throws ProcedureException {
    }

}
