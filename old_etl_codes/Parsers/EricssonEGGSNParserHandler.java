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
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author TTGETERZI
 */
public class EricssonEGGSNParserHandler extends SaxParserHandler {

    public EricssonEGGSNParserHandler(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    String fileDate;
    String neName;
    String gp = "0";
   // String singleCOunterName;

    boolean isdateSetted = false;
    boolean isNeNameSetted = false;
  //  boolean isCounterNameSetted = false;
    String currentTagValue;

    EricssonEGGSNNewSubset subset;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentTagValue = "";
        
        switch (qName) {
            case "mi":
                subset = new EricssonEGGSNNewSubset();
                break;
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        currentTagValue += new String(ch, start, length);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case "cbt":
                if (isdateSetted == false) {
                    fileDate = currentTagValue.substring(0, currentTagValue.length() - 2);

                    isdateSetted = true;
                }
                break;
            case "neun":
                if (isNeNameSetted == false) {
                    neName = currentTagValue;
                    isNeNameSetted = true;
                }
                break;
            case "gp":
                gp = currentTagValue;
                break;
            case "mt":
                subset.addHeader(currentTagValue.trim());
                break;
            case "moid":
                subset.setMeasObjLdn(currentTagValue);
                subset.setMeasObjectOperation(currentTagValue);
                break;
            case "r":
                subset.addValue(currentTagValue);
                break;
            case "mv":
                writeIntoFile();
                subset.reset();
                break;
            case "mi":
                subset = null;
                gp = "0";
                break;
        }
    }

    private void writeIntoFile() {
        RawTableObject object = TableWatcher.getInstance().getTableObjectFromCounterName(Arrays.asList(subset.getVariableFullHeader().split("\\|")));
        if (object != null) {
            String FullHeader = "GGSN_NAME|DATA_DATE|GP|" + subset.getVariableFullHeader();
            String fullValue = neName + "|" + fileDate + "|" + gp + "|" + subset.getVariableFullValues();
            String value = (CommonLibrary.get_RecordValue(FullHeader, fullValue,
                    object.getFullColumnOrderUsingCounterNameFil("|"), "0", "|", "|"));
            String outputFileName = AbsParserEngine.LOCALFILEPATH + object.getTableName() + AbsParserEngine.integratedFileExtension;

            value = value.replace("NULL", "0").replace("null", "0");
            try {
                writeIntoFilesWithController(outputFileName, value + "\n");
            } catch (ParserIOException ex) {
                Logger.getLogger(EricssonEGGSNParserHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void onStartParseOperation() {
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onstopParseOperation() {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
