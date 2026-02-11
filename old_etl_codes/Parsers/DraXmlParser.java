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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author TurgutSimsek
 */
public class DraXmlParser extends SaxParserHandler {

    private String tagValue;
    DraNewSubset subset;
    private final SimpleDateFormat dateFormatterRaw = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final SimpleDateFormat dateOutputFormatForResultFile = new SimpleDateFormat("yyyyMMddHHmm");
    private Date dateAsDate;
    boolean isDateSetted = false;
    private String dateAsStr;
    private boolean isNeNameSetted;
    private String draStpName;

    public DraXmlParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void onStartParseOperation() {

    }

    @Override
    public void onstopParseOperation() {

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        tagValue = "";

        switch (qName) {

            case "measInfo":
                subset = new DraNewSubset();
                String functionSubsetName = attributes.getValue("measInfoId");
                subset.setFunctionSubsetName(functionSubsetName);
                break;
            case "measCollec":
                if (isDateSetted == false) {
                    dateAsStr = attributes.getValue("beginTime");
                    dateAsStr = dateAsStr.split("\\+")[0].replace("T", " ");
                    try {
                        dateAsDate = dateFormatterRaw.parse(dateAsStr);
                    } catch (ParseException ex) {
                    }
                    isDateSetted = true;
                }
                break;
            case "managedElement":
                if (isNeNameSetted == false) {
                    draStpName = attributes.getValue("localDn").split("\\=")[1];
                }
                break;
            case "measValue":
                subset.reset();
                String measObjet = attributes.getValue("measObjLdn");
                if (measObjet.equals("")) {
                    measObjet = "0";
                }
                subset.addProperty("DRASTP_NAME", draStpName);
                subset.addProperty("DATA_DATE", dateAsStr);
                subset.addProperty("MOID", measObjet);
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

        }

    }

    private void writeIntoFiles() {
        RawTableObject tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(subset.getFunctionSubsetName());
        //  System.out.println("function subset name " + subset.getFunctionSubsetName());

        if (tableObject != null) {
            //  System.out.println("raw table object " + tableObject.getTableName());
            if (dateAsStr != null) {
                String myColums = tableObject.getFullColumnOrderUsingCounterNameFil("|");
                String fullHeader = subset.getVariableFullHeader();
                String fullValues = subset.getVariableFullValues();

                //   System.out.println("full header " + fullHeader);
                //   System.out.println("full values " + fullValues);
                String myLine = CommonLibrary.get_RecordValue(fullHeader, fullValues, myColums, "", "|", "|");

                String fileOutputName = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;

                try {
                    writeIntoFilesWithController(fileOutputName, myLine + "\n");
                } catch (ParserIOException ex) {

                }

            }
        }

    }

}
