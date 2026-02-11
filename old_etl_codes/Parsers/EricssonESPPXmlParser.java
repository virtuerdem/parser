/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_Ericsson_spp;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.exceptions.ParserIOException;
import com.ttgint.parserEngine.parserHandler.SaxParserHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author EnesTerzi
 */
public class EricssonESPPXmlParser extends SaxParserHandler {
    
    public EricssonESPPXmlParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }
    
    @Override
    public void onStartParseOperation() {
       // System.out.println(currentFileProgress.getName());
    }
    
    @Override
    public void onstopParseOperation() {
        //     throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    private String constantValue;
    private String constantHeader = "SPP_NAME|DATA_DATE";
    
    private boolean flag_isDateSetted = false;
    private boolean flag_isNeNameSetted = false;
    private String fileDate;
    private String ne_Name;
    private String measObjLdn;
    private boolean flag_istableNeed = false;
    
    private EricssonESPPFunctionSubSet subset;
    private String outputDate;
    
    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tagValue = "";
        
        switch (qName) {
            case "measCollec":
                if (flag_isDateSetted == false) {
                    String date = attributes.getValue("beginTime");
                    fileDate = date.replace("T", " ").split("\\+")[0];
                    constantValue = constantValue + "|" + fileDate;
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date dateformat = null;
                    try {
                        dateformat = sdf.parse(fileDate);
                    } catch (ParseException ex) {
                        System.out.println("Date Exception");
                        System.exit(1);
                    }
                    SimpleDateFormat sdf2 = new SimpleDateFormat("yyyyMMddHHmm");
                    outputDate = sdf2.format(dateformat);
                 //   System.out.println(outputDate);
                    flag_isDateSetted = true;
                }
                break;
            case "fileSender":
                if (flag_isNeNameSetted == false) {
                    ne_Name = attributes.getValue("localDn").replace("ManagedElement=", "");
                    constantValue = ne_Name;
                    flag_isNeNameSetted = true;
                }
                break;
            case "measInfo":
                subset = null;
                subset = new EricssonESPPFunctionSubSet();
                break;
            case "measValue":
                measObjLdn = attributes.getValue("measObjLdn");
                String tableName = measObjLdn.split(",")[0].toUpperCase().trim();
              //  System.out.println(tableName);
                if (ParserEngine_pm_Ericsson_spp.sppRawTableNames.contains(tableName)) {
                    subset.setFileHeader(constantHeader);
                    subset.setFileValues(constantValue);
                    subset.setMeastypeValues("");
                    subset.setTableName(tableName);
                    parsemeasObjLdn();
                    flag_istableNeed = true;
                    
                }
                break;
        }
    }
    
    private String tagValue;
    
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tagValue += new String(ch, start, length);
    }
    
    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case "measType":
                subset.setMeasTypeHeader(subset.getMeasTypeHeader() + "|"
                        + tagValue.toUpperCase());
                break;
            case "measValue":
                if (flag_istableNeed) {
                    String tableheader = subset.getTableHeader();
                    String fileHeader = (subset.getFileHeader() + subset.getMeasTypeHeader());
                    String fileVaules = (subset.getFileValues() + subset.getMeastypeValues());
                    
                    String line = com.ttgint.parserEngine.commonLibrary.CommonLibrary.get_RecordValue(fileHeader, fileVaules, tableheader, "0", "|", "|");
                    
                    subset.setMeastypeValues("");
                     line = line + "\n";
            try {
                writeIntoFilesWithController(AbsParserEngine.LOCALFILEPATH + subset.getTableName() + AbsParserEngine.integratedFileExtension, line);
            } catch (ParserIOException ex) {
                Logger.getLogger(EricssonESPPXmlParser.class.getName()).log(Level.SEVERE, null, ex);
            }
                    flag_istableNeed = false;
                }
                
                break;
            case "r":
                subset.setMeastypeValues(subset.getMeastypeValues() + "|" + tagValue);
                
                break;
            
        }
    }
    
    private void parsemeasObjLdn() {
        String[] splitttedMeas = measObjLdn.split(",");
        if (splitttedMeas.length > 1) {
            for (int i = 1; i < splitttedMeas.length; i++) {
                String[] splittedeach = splitttedMeas[i].split("\\=");
                subset.setFileHeader(subset.getFileHeader() + "|" + splittedeach[0].toUpperCase());
                subset.setFileValues(subset.getFileValues() + "|" + splittedeach[1]);
            }
        }
        
    }
    
}
