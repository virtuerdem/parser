/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.parserHandler.FileReaderHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_MSP.kpiNameInfos;
import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_MSP.fileNameInfos;
import static com.ttgint.parserEngine.common.AbsParserEngine.dbHelper;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author ibrahim.egerci
 */
public class MspPmXmlHandler extends FileReaderHandler {

    private File xmlFile;
    private RawTableObject tableObject;

    private String fileName;
    private String networkName;
    private String nodeName;
    private String neType;
    private String kpiGroup;
    private String kpiName;
    private String kpiNameId;
    private String kpiGroupId;
    private String filePdp;
    private Date fileLastWork;

    private boolean validPeriod = false;
    private boolean database = false;
    private String measurementType;
    private String cf;
    private double value;
    private HashMap<Date, String> allHashMap = new HashMap<>();
    private HashMap<Date, Double> averageHashMap = new HashMap<>();
    private HashMap<Date, Double> minHashMap = new HashMap<>();
    private HashMap<Date, Double> maxHashMap = new HashMap<>();
    private ArrayList<Date> workDate = new ArrayList<Date>();

    private SimpleDateFormat formatterZone = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z");
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public MspPmXmlHandler(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
        this.xmlFile = currentFileProgress;
    }

    @Override
    public void onStartParseOperation() {
        fileName = xmlFile.getName();
        networkName = fileName.split("\\-")[0];
        nodeName = fileName.split("\\-")[1];
        neType = fileName.split("\\-")[1].replaceAll("[0-9]", "");
        kpiName = fileName.replace(networkName + "-", "").replace(nodeName + "-", "").replace(".xml", "");
        kpiGroup = fileName.split("\\-")[2];

        kpiNameId = kpiNameInfos.get(neType + "-" + kpiName).get(0);
        kpiGroupId = kpiNameInfos.get(neType + "-" + kpiName).get(1);
        filePdp = fileNameInfos.get(fileName.replace(".xml", "")).get(0);
        try {
            fileLastWork = formatter.parse(fileNameInfos.get(fileName.replace(".xml", "")).get(1));
        } catch (Exception e) {
        }

        tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetNameAndNeType(kpiGroup, neType);
    }

    @Override
    public void lineProgress(String line) {
        String trimLine = line.trim();

        if (trimLine.startsWith("<type>")) {
            measurementType = lineCleaner(trimLine, "type");
        }

        if (trimLine.startsWith("<cf>")) {
            cf = lineCleaner(trimLine, "cf");
        }

        if (trimLine.startsWith("<pdp_per_row>")) {
            if (lineCleaner(line, "pdp_per_row").equals(filePdp)) {
                validPeriod = true;
            } else {
                validPeriod = false;
            }
            return;
        }

        if (validPeriod) {

            if (trimLine.startsWith("</database>")) {
                database = false;
            }

            if (database) {
                String timeZone = trimLine.split(" ")[3];
                String date = trimLine.split(" ")[1] + " " + trimLine.split(" ")[2];
                value = Double.parseDouble(lineCleaner(line.split("\\ ")[line.split("\\ ").length - 1], "v").replace("NaN", "0"));

                try {
                    Date currentDate = new Date();
                    if (timeZone.equals("UTC")) {
                        currentDate = formatterZone.parse(date + " " + timeZone);
                    } else {
                        currentDate = formatter.parse(date);
                    }

                    if (currentDate.after(fileLastWork)) {
                        switch (cf) {
                            case "AVERAGE":
                                String valueAll = formatter.format(currentDate) + AbsParserEngine.resultParameter
                                        + networkName + AbsParserEngine.resultParameter
                                        + nodeName + AbsParserEngine.resultParameter
                                        + kpiName + AbsParserEngine.resultParameter
                                        + kpiNameId + AbsParserEngine.resultParameter
                                        + measurementType + AbsParserEngine.resultParameter
                                        + filePdp + AbsParserEngine.resultParameter
                                        + kpiGroupId;

                                allHashMap.put(currentDate, valueAll);
                                averageHashMap.put(currentDate, value);
                                break;
                            case "MAX":
                                maxHashMap.put(currentDate, value);
                                break;
                            case "MIN":
                                minHashMap.put(currentDate, value);
                                break;
                        }
                    }
                } catch (Exception e) {
                }
            }

            if (trimLine.startsWith("<database>")) {
                database = true;
            }

            if (trimLine.startsWith("</rra>")) {
                if (!averageHashMap.isEmpty() && !minHashMap.isEmpty() && !maxHashMap.isEmpty()) {
                    String fullPath = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;
                    for (Date each : allHashMap.keySet()) {
                        if (averageHashMap.containsKey(each) && maxHashMap.containsKey(each) && minHashMap.containsKey(each)) {
                            try {
                                String fileHeader = "dataDate" + AbsParserEngine.resultParameter
                                        + "networkName" + AbsParserEngine.resultParameter
                                        + "nodeName" + AbsParserEngine.resultParameter
                                        + "kpiName" + AbsParserEngine.resultParameter
                                        + "kpiNameId" + AbsParserEngine.resultParameter
                                        + "measurementType" + AbsParserEngine.resultParameter
                                        + "filePdp" + AbsParserEngine.resultParameter
                                        + "kpiGroupId" + AbsParserEngine.resultParameter
                                        + "kpi_" + kpiNameId + "_avg" + AbsParserEngine.resultParameter
                                        + "kpi_" + kpiNameId + "_max" + AbsParserEngine.resultParameter
                                        + "kpi_" + kpiNameId + "_min";

                                String record = allHashMap.get(each) + AbsParserEngine.resultParameter
                                        + averageHashMap.get(each) + AbsParserEngine.resultParameter
                                        + maxHashMap.get(each) + AbsParserEngine.resultParameter
                                        + minHashMap.get(each);

                                record = CommonLibrary.get_RecordValue(fileHeader, record, tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter), "0", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter);
                                workDate.add(each);
                                writeIntoFilesWithController(fullPath, record + "\n");
                            } catch (Exception ex) {
                                System.out.println("Error line: " + ex.getMessage());
                            }
                        }
                    }

                    try {
                        String lastWorkDate = formatter.format(workDate.stream().max(Comparator.naturalOrder()).get());
                        dbHelper.setKpiNameandLastWorkTime(AbsParserEngine.systemType, AbsParserEngine.measType, AbsParserEngine.operatorName, fileName.replace(".xml", ""), lastWorkDate);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    workDate.clear();
                    allHashMap.clear();
                    averageHashMap.clear();
                    maxHashMap.clear();
                    minHashMap.clear();
                    requestStop();
                }
            }
        }
    }

    @Override
    public void onstopParseOperation() {
    }

    public String lineCleaner(String line, String tag) {
        return line.substring(line.indexOf("<" + tag + ">") + tag.length() + 2, line.indexOf("</" + tag + ">")).trim();
    }

}
