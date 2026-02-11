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
import com.ttgint.parserEngine.parserHandler.SaxParserHandler;
import com.ttgint.parserEngine.systemProperties.OperationSystemEnum;
import com.ttgint.parserEngine.systemProperties.ProgressTypeEnum;
import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author Administrator
 */
public class PcrfPmXmlParser extends SaxParserHandler {

    private String currentTagValue;
    boolean rowFlag;
    boolean fileStart = false;
    int funcNameIndex = 0;
    String fileContentDataDate = "";
    PcrfPmXmlParserSubset subset = null;
    SimpleDateFormat sdfd = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    SimpleDateFormat sdfs = new SimpleDateFormat("yyyyMMddHHmm");

    public PcrfPmXmlParser(File currentFileProgress, List<String> functionSubsetNameList, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);

    }

    @Override
    public void onStartParseOperation() {

    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentTagValue = "";
        
        switch (qName) {

            case "Statistics":
                fileStart = true;
                subset = new PcrfPmXmlParserSubset();
                break;

            case "Sample":
                rowFlag = true;
                break;

        }

        if (fileStart) {
            ++funcNameIndex;
            if (fileStart && funcNameIndex == 2) {
                subset.setFunctionSubsetName(qName);
                fileStart = false;
            }
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        currentTagValue += new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        switch (qName) {

            case "Statistics":
                fileStart = false;
                break;

            case "StartTime": {
                try {
                    subset.addHeader(qName);
                    subset.addValues(sdfs.format(sdfd.parse(currentTagValue)));
                    fileContentDataDate = sdfs.format(sdfd.parse(currentTagValue));
                } catch (ParseException ex) {
                    Logger.getLogger(PcrfPmXmlParser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;

            case "EndTime": {
                try {
                    subset.addHeader(qName);
                    subset.addValues(sdfs.format(sdfd.parse(currentTagValue)));
                } catch (ParseException ex) {
                    Logger.getLogger(PcrfPmXmlParser.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;

            case "Sample":
                rowFlag = false;
                RawTableObject tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(subset.getFunctionSubsetName());
                if (tableObject != null) {
                    String tableColumnNames = tableObject.getFullColumnOrderUsingCounterNameFil(AbsParserEngine.resultParameter);
                    try {
                        String fullPath = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;

                        writeIntoFilesWithController(fullPath, CommonLibrary.get_RecordValue(subset.getFullHeader(), subset.getFullValues(), tableColumnNames, "0", AbsParserEngine.resultParameter, AbsParserEngine.resultParameter) + "\n");
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                    }
                }
                subset.reset();
                break;

            default:
                if (rowFlag) {
                    if (qName.equals("MRA")) {
                        qName = "PolicyServer";
                    }
                    subset.addHeader(qName);
                    subset.addValues(currentTagValue.trim());
                }
                break;
        }

    }

    @Override
    public void onstopParseOperation() {

    }

}
