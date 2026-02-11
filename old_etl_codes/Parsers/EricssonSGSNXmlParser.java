/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
import com.ttgint.parserEngine.exceptions.ParserIOException;
import com.ttgint.parserEngine.parserHandler.SaxParserHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author TTGETERZI
 */
public class EricssonSGSNXmlParser extends SaxParserHandler {

    public EricssonSGSNXmlParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void onStartParseOperation() {

    }

    boolean isDateSetted = false;
    boolean isNeNameSetted = false;

    private String sgsnName;
    private String tagValue;
    private String dateAsStr;
    private String measType;

    private EricssonEGGSNNewSubset subset;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tagValue = "";

        switch (qName) {
            case "measInfo":
                subset = new EricssonEGGSNNewSubset();
                measType = attributes.getValue("measInfoId");
                break;
            case "measCollec":
                if (isDateSetted == false) {
                    dateAsStr = attributes.getValue("beginTime");
                    dateAsStr = dateAsStr.split("\\+")[0].replace("T", " ");
                    isDateSetted = true;
                }
                break;
            case "managedElement":
                if (isNeNameSetted == false) {
                    sgsnName = attributes.getValue("localDn");
                    isNeNameSetted = true;
                }
                break;
            case "measValue":
                subset.reset();
                String measObjet = attributes.getValue("measObjLdn");
                if (measObjet.equals("")) {
                    measObjet = "0";
                } else {
                    measObjet = measObjet.substring(measObjet.indexOf(",") + 1);
                }
                subset.setMeasObjLdn(measObjet);
                subset.setMeasObjectOperation(measObjet);
                subset.addProperty("MOID", measObjet);
                subset.addProperty("SGSN_NAME", sgsnName);
                subset.addProperty("DATA_DATE", dateAsStr);
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        tagValue += new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case "r":
                subset.addValue(tagValue);
                break;
            case "measType":
                subset.addHeader(tagValue);
                break;
            case "measValue":
                writeIntoFiles();
                break;
            case "suspect":
//                subset.setSuspect(Boolean.parseBoolean(tagValue));
                break;
        }
    }

    @Override
    public void onstopParseOperation() {

    }

    private void writeIntoFiles() {
        RawTableObject tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(measType);
        if (tableObject != null) {
            if (subset.isSuspect() == false && dateAsStr != null) {
                String myColums = tableObject.getFullColumnOrderUsingCounterNameFil("|");
                String fullHeader = subset.getVariableFullHeader();
                String fullValues = subset.getVariableFullValues();
//                System.out.println(fullHeader);
//                System.out.println(fullValues);
//                System.out.println(myColums);
                String myLine = CommonLibrary.get_RecordValue(fullHeader, fullValues.replace("|NIL|", "|0|").replace("|NIL", "|0").replace("NIL|", "0|"), myColums, "0", "|", "|");

                String fileOutputName = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;

                try {
                    writeIntoFilesWithController(fileOutputName, myLine + "\n");
                } catch (ParserIOException ex) {

                }

            }
        }

    }

}
