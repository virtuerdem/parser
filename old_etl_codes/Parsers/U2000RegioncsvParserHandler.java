/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.io.File;
import com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_xls_u2000_region_Parser;
import com.ttgint.parserEngine.parserHandler.CsvFileHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;

/**
 *
 * @author TTGETERZI
 */
public class U2000RegioncsvParserHandler extends CsvFileHandler {

    public U2000RegioncsvParserHandler(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void onStartParseOperation() {

    }

    @Override
    public void onstopParseOperation() {
        System.out.println(size);

    }
    int size = 0;

    boolean flag = false;

    @Override
    public void lineProgress(String[] line) {
        String deviceName = line[0].trim();

        if (deviceName.equals("NE Name")) {
            flag = true;
            return;
        }
        if (flag) {

        

            if (deviceName.contains("RTN") && deviceName.contains("PTN") == false) {
                String regiod = line[11];
                //    System.out.println(regiod);
                if (deviceName.equals("D1824-NE4496_KOCIBEY_CAD")) {
                    System.out.println("before region : " + regiod);
                }

                regiod = parseRegion(regiod);
                if (deviceName.equals("D1824-NE4496_KOCIBEY_CAD")) {
                    System.out.println("after region : " + regiod);
                }

                // System.out.println(regiod);
                if (regiod != null) {
                    if (ParserEngine_xls_u2000_region_Parser.objectList.keySet().contains(deviceName)) {
                        System.out.println("here");
                    } else {
                        size++;
                        ParserEngine_xls_u2000_region_Parser.objectList.put(deviceName, regiod);
                    }
                } else {

                }
            }
        }

    }

    private String parseRegion(String region) {
        String value;
        if (region.contains("IC_EGE")) {
            value = "IC EGE";
        } else if (region.contains("ISTANBUL(AVRUPA)")) {
            value = "ISTANBUL AVRUPA";
        } else if (region.contains("ISTANBUL(TRAKYA)")) {
            value = "ISTANBUL TRAKYA";
        } else if (region.contains("ISTANBUL(AVRUPA)")) {
            value = "ISTANBUL AVRUPA";
        } else if (region.contains("ISTANBUL(ASYA)")) {
            value = "ISTANBUL ASYA";
        } else if (region.contains("BATI KARADENIZ")) {
            value = "BATI KARADENIZ";
        } else if (region.contains("DOGU KARADENIZ")) {
            value = "DOGU KARADENIZ";
        } else if (region.contains("EGE")) {
            value = "EGE";
        } else if (region.contains("iC ANADOLU")) {
            value = "IC ANADOLU";
        } else if (region.contains("DOGU KARADENIZ")) {
            value = "DOGU KARADENIZ";
        } else if (region.contains("GUNEY ANADOLU 2")) {
            value = "GUNEY ANADOLU 2";
        } else if (region.contains("GUNEY DOGU ANADOLU")) {
            value = "GUNEY DOGU ANADOLU";
        } else if (region.contains("GUNEY ANADOLU")) {
            value = "GUNEY ANADOLU";
        } else if (region.contains("ORTA ANADOLU")) {
            value = "ORTA ANADOLU";
        } else if (region.contains("DOGU ANADOLU")) {
            value = "DOGU ANADOLU";
        } else if (region.contains("AKDENIZ")) {
            value = "AKDENIZ";
        } else if (region.contains("GUNEY MARMARA")) {
            value = "GUNEY MARMARA";
        } else if (region.contains("VODAFONE NET")) {
            value = null;
        } else {
            value = null;
        }
        return value;

    }

}
