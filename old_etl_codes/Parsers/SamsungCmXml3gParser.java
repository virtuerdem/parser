/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_cm_xml_Samsung;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.parserHandler.SaxParserHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author TTGETERZI
 */
public class SamsungCmXml3gParser extends SaxParserHandler {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    private final SamsungCmXmlMainSubset topParentSubset;

    private String ipAdress;
    private String currentTagValue;
    private SamsungCmXml3gSubset currentSub;
    private boolean isRncSecToLCidInfStarted = false;
    private boolean isRncCellInf = false;
    private final HashMap<String, SamsungCmXml3gSubset> userCellIdtoObject;

    public SamsungCmXml3gParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType, SamsungCmXmlMainSubset subset) {
        super(currentFileProgress, operationSystem, progType);
        topParentSubset = subset;
        userCellIdtoObject = new HashMap<>();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        currentTagValue += new String(ch, start, length).trim();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (strategyRncSecToLcId) {
            if (isRncSecToLCidInfStarted) {
                switch (qName) {
                    case "en:RncSecToLCidInf":
                        if (currentSub.getStatus().equals("1")) {
                            userCellIdtoObject.put(currentSub.getUserCellId(), currentSub);

                        }
                        currentSub = null;
                        isRncSecToLCidInfStarted = false;
                        break;
                    case "en:userCellId":

                        currentSub.setUserCellId(currentTagValue);

                        break;
                    case "en:status":

                        currentSub.setStatus(currentTagValue);

                        break;
                    case "en:bsSysId":

                        currentSub.setBsSysId(currentTagValue);

                        break;
                }
            }
            if (isRncCellInf) {
                switch (qName) {
                    case "en:userCellId":
                        if (currentTagValue.startsWith("-") == false) {
                            currentSub = userCellIdtoObject.get(currentTagValue);
                        }
                        break;
                    case "en:UserLabel":
                        if (currentSub != null) {
                            currentSub.setUserLabel(currentTagValue);
                        }
                        break;
                    case "en:RncCellInf":
                        if (currentSub != null) {

                            finalJob(currentSub);
                            userCellIdtoObject.remove(currentSub.getUserCellId());
                        }
                        currentSub = null;
                        isRncCellInf = false;
                        break;

                }
            }
        }

        if (strategyRncCellInf) {
            // System.out.println(currentTagValue);
            if (isRncSecToLCidInfStarted) {
                switch (qName) {
                    case "en:RncSecToLCidInf":
                        if (currentSub != null
                                && currentSub.getStatus().equals("1")) {
                            finalJob(currentSub);
                        }
                        break;
                    case "en:userCellId":
                        currentSub = userCellIdtoObject.get(currentTagValue);
                        break;
                    case "en:status":
                        if (currentSub != null) {
                            currentSub.setStatus(currentTagValue);
                        }
                        break;
                    case "en:bsSysId":
                        if (currentSub != null) {
                            currentSub.setBsSysId(currentTagValue);
                        }
                        break;
                }
            }

            if (isRncCellInf) {
                switch (qName) {
                    case "en:userCellId":
                        if (currentSub != null) {
                            currentSub.setUserCellId(currentTagValue);
                        }
                        break;
                    case "en:UserLabel":
                        if (currentSub != null) {
                            currentSub.setUserLabel(currentTagValue);
                        }
                        break;
                    case "en:RncCellInf":
                        if (currentSub.getUserCellId().startsWith("-") == false) {
                            userCellIdtoObject.put(currentSub.getUserCellId(), currentSub);

                        }
                        //  currentSub = null;
                        break;

                }
            }

        }

    }
    boolean firstCheck = false;
    boolean strategyRncCellInf;
    boolean strategyRncSecToLcId;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentTagValue = "";
        
        switch (qName) {
            case "en:RncSecToLCidInf":
                if (firstCheck == false) {
                    strategyRncSecToLcId = true;
                    firstCheck = true;
                }
                if (strategyRncSecToLcId) {
                    currentSub = new SamsungCmXml3gSubset();
                }
                isRncSecToLCidInfStarted = true;
                break;
            case "en:RncCellInf":
                if (firstCheck == false) {
                    strategyRncCellInf = true;
                    firstCheck = true;
                }
                if (strategyRncCellInf) {
                    currentSub = new SamsungCmXml3gSubset();
                }
                isRncCellInf = true;
                break;
        }
    }

    private int cnumCounter = 0;

    private void finalJob(SamsungCmXml3gSubset subsetObject) {

        subsetObject.GenerateIds(topParentSubset.getNeId(), cnumCounter);
        cnumCounter++;
        try {

            FileOutputStream cnumOutput = new FileOutputStream(new File(AbsParserEngine.LOCALFILEPATH + ParserEngine_cm_xml_Samsung.RNCCellMapTABLE_NAME + AbsParserEngine.integratedFileExtension), true);
            String line = subsetObject.getGeneratedCnum() + "|" + subsetObject.getNetworkId() + "|" + sdf.format(new Date()) + "|37\n";
            cnumOutput.write((line.getBytes()));
            cnumOutput.close();

            FileOutputStream output = new FileOutputStream(new File(AbsParserEngine.LOCALFILEPATH + ParserEngine_cm_xml_Samsung.TABLE_NAME_3G + AbsParserEngine.integratedFileExtension), true);
            output.write((subsetObject.toString() + sdf.format(new Date()) + "\n").getBytes());
            output.close();
        } catch (FileNotFoundException ex) {

        } catch (IOException ex) {

        }
    }

    @Override
    public void onStartParseOperation() {
        String newId = ParserEngine_cm_xml_Samsung.neNameToIdList.get(topParentSubset.getNeName());
        ipAdress = currentFileProgress.getName().split("\\+")[0];
        topParentSubset.setNeId(newId);
        topParentSubset.generateId(sdf.format(new Date()), ipAdress);
        //  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onstopParseOperation() {
//        System.out.println(strategyRncCellInf);
//        System.out.println(strategyRncSecToLcId);
    }

}
