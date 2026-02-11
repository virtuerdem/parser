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
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author TTGETERZI
 */
public class SamsungCmXml2gParser extends SaxParserHandler {
    
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    private final SamsungCmXmlMainSubset topParentSubset;
    
    private String ipAdress;
    private String currentTagValue;
    private SamsungCmXml2gSubset currentSub;
    private boolean isObjectstarted = false;
    
    public SamsungCmXml2gParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType, SamsungCmXmlMainSubset subset) {
        super(currentFileProgress, operationSystem, progType);
        this.topParentSubset = subset;
    }
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        currentTagValue += new String(ch, start, length).trim();
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (isObjectstarted) {
            switch (qName) {
                case "en:BscCellInfo":
                    finalJob(currentSub);
                    currentSub = null;
                    isObjectstarted = false;
                    break;
                case "en:userCellId":
                    currentSub.setUserCellId(currentTagValue);
                    break;
                case "en:ValidFlag":
                    currentSub.setValidFlag(currentTagValue);
                    break;
                case "en:bsSysId":
                    currentSub.setBsSysId(currentTagValue);
                    break;
                case "en:UserLabel":
                    currentSub.setUserLabel(currentTagValue);
                    break;
            }
        }
    }
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentTagValue = "";
        
        switch (qName) {
            case "en:BscCellInfo":
                currentSub = new SamsungCmXml2gSubset();
                isObjectstarted = true;
                break;
        }
    }
    
    private void finalJob(SamsungCmXml2gSubset subsetObject) {
        if (subsetObject.getValidFlag().equals("1")) {
            
            subsetObject.GenerateIds(topParentSubset.getNeId());
            try {
                FileOutputStream output = new FileOutputStream(new File(AbsParserEngine.LOCALFILEPATH + ParserEngine_cm_xml_Samsung.TABLE_NAME_2G + AbsParserEngine.integratedFileExtension), true);
                output.write((subsetObject.toString() + sdf.format(new Date()) + "\n").getBytes());
                output.close();
            } catch (FileNotFoundException ex) {
                
            } catch (IOException ex) {
                
            }
        }
    }
    
    @Override
    public void onStartParseOperation() {
        String newId = ParserEngine_cm_xml_Samsung.neNameToIdList.get(topParentSubset.getNeName());
        topParentSubset.setNeId(newId);
        ipAdress = currentFileProgress.getName().split("\\+")[0];
        topParentSubset.generateId(sdf.format(new Date()), ipAdress);
    }
    
    @Override
    public void onstopParseOperation() {
        //   throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
