/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_cm_xml_Samsung;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.parserHandler.FileHandler;
import com.ttgint.parserEngine.parserHandler.SaxParserHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author TTGETERZI
 */
public class SamsungCmXmlMainParserNew extends SaxParserHandler {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    private String ipAdress;
    private final ArrayList<SamsungCmXmlMainSubset> subsetList;

    private String currentTagValue;
    private SamsungCmXmlMainSubset currentSubset;

    public SamsungCmXmlMainParserNew(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
        subsetList = new ArrayList<>();
    }

    @Override
    public void onStartParseOperation() {
        String fileName = currentFileProgress.getName();
        ipAdress = fileName.split("\\+")[0];

    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        currentTagValue += new String(ch, start, length);
        currentTagValue = currentTagValue.trim();
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case "en:neGroup":
                currentSubset.setNeGroup(currentTagValue);
                break;
            case "en:neVersion":
                currentSubset.setNeVersion(currentTagValue);
                break;
            case "en:neId":
                currentSubset.setNeId(currentTagValue);
                break;
            case "en:neName":
                currentSubset.setNeName(currentTagValue);
                break;
            case "en:neIp":
                currentSubset.setNeIp(currentTagValue);
                break;
            case "en:neType":
                currentSubset.setNeType(currentTagValue);
                break;
            case "en:gsmParent":
                currentSubset.setGsmParent(currentTagValue);
                break;
            case "en:wcdmaParent":
                currentSubset.setWcdmaParent(currentTagValue);
                break;
            case "en:ManagementNode":
                subsetList.add(currentSubset);
                currentSubset = null;
                break;

        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentTagValue = "";

        switch (qName) {
            case "en:ManagementNode":
                currentSubset = new SamsungCmXmlMainSubset();
                break;
        }
    }

    @Override
    public void onstopParseOperation() {
        beforeParser();

        SAXParserFactory factory = SAXParserFactory.newInstance();
        for (SamsungCmXmlMainSubset each : subsetList) {
            String fileName = null;
            FileHandler handler = null;

            switch (each.getNeType()) {

                case "bsc":
                    fileName = AbsParserEngine.LOCALFILEPATH + ipAdress + "+" + "BSC_" + each.getNeId() + "_gsm.xml";
                    handler = new SamsungCmXml2gParser(new File(fileName), OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT, each);
                    break;
                case "rnc":
                    fileName = AbsParserEngine.LOCALFILEPATH + ipAdress + "+" + "RNC_" + each.getNeId() + "_wcdma.xml";
                    if (each.getNeVersion().equals("3.1.0")) {
                        handler = new SamsungCmXml3gParser(new File(fileName), OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT, each);
                    } else {
//                        handler = new Samsung3gXmlParser(new File(fileName), OperationSystemEnum.WINDOWS, ProgressTypeEnum.PRODUCT, each);
                    }
                    break;
            }
//            System.out.println(handler);
//            System.out.println(fileName);
            if (handler != null && fileName != null) {
                File file = new File(fileName);

                if (file.exists()) {
                    handler.run();
                }

            }
        }

    }

    private String idChanger(String neName) {
        String newId = ParserEngine_cm_xml_Samsung.neNameToIdList.get(neName);
        if (newId == null) {
            try {
                int newIdFromSeq = AbsParserEngine.dbHelper.getNextValueFromSequence("NORTHI_PARSER_SETTINGS.SEQ_SAMSUNGNE");
                String systemType = neName.contains("BSC") ? "SAM2G" : "SAM3G";
                String neType = neName.contains("BSC") ? "BSC" : "RNC";
                String query = "insert into northi_parser_settings.parser_used_nes(ne_name,raw_ne_id,system_type,ne_type ,operator_name,is_active) "
                        + "select '" + neName + "'," + newIdFromSeq + ",'" + systemType + "','" + neType + "','VODAFONE',1 from dual ";
                AbsParserEngine.dbHelper.executeQueryDirect(query);
                synchronized (ParserEngine_cm_xml_Samsung.neNameToIdList) {
                    ParserEngine_cm_xml_Samsung.neNameToIdList.put(neName, String.valueOf(newIdFromSeq));
                }
                newId = String.valueOf(newIdFromSeq);
                System.out.println("New element name " + neName + " id : " + newIdFromSeq);

            } catch (Exception ex) {
                //Logger.getLogger(SamsungCmXmlMainParserNew.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            synchronized (ParserEngine_cm_xml_Samsung.copyNeList) {
                if (ParserEngine_cm_xml_Samsung.copyNeList.contains(neName)) {
                    ParserEngine_cm_xml_Samsung.copyNeList.remove(neName);
                }
            }
        }
        return newId;
    }

    private void beforeParser() {
        for (SamsungCmXmlMainSubset each : subsetList) {
            if (each.getNeType().equals("mbs")) {
                if (!each.getGsmParent().equals("N/A")) {
                    String neName = findBscOrRncName(each.getGsmParent());
                    String newNeId = idChanger(neName);
                    each.setGsmParentdirect(newNeId);

                }
                if (!each.getWcdmaParent().equals("N/A")) {
                    String neName = findBscOrRncName(each.getWcdmaParent());
                    String newNeId = idChanger(neName);
                    each.setWcdmaParentdirect(newNeId);

                }
                each.generateId(sdf.format(new Date()), ipAdress);

            }
        }
    }

    private String findBscOrRncName(String id) {
        String neName = null;
        for (SamsungCmXmlMainSubset each : subsetList) {
            if (each.getNeType().equals("bsc") || each.getNeType().equals("rnc")) {
                if (each.getNeId().equals(id)) {
                    neName = each.getNeName();
                    break;
                }
            }
        }
        return neName;
    }

}
