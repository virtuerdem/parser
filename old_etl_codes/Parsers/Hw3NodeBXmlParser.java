/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_xml_3G_LocalCell;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.AutoCounterDefine;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

/**
 *
 * @author TTGETERZI
 */
public class Hw3NodeBXmlParser extends SaxParserHandler {

    public Hw3NodeBXmlParser(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    private String rncName = null;
    private String rncId = null;
    private String topParentId = null;
    private String nodebName;
    private String currentTagValue;
    private String beginTime;
    private boolean isDateSetted = false;
    private Date dateObject;
    private SimpleDateFormat dateFormatFromRawFile = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private SimpleDateFormat dateFormatFromRawFileZ = new SimpleDateFormat("yyyyMMdd.HHmmZ");
    private SimpleDateFormat dateFormatResultFiel = new SimpleDateFormat("yyyyMMddHHmm");

    Hw3gNodeBXmlSubset currentSubet = null;

    @Override
    public void onStartParseOperation() {
        String currentFileName = (currentFileProgress.getName());
        String[] splittedFile = currentFileName.split("\\_");
        rncName = splittedFile[1].split("\\.")[0].trim();
        rncId = ParserEngine_xml_3G_LocalCell.neNameToNerawId.get(rncName);
        topParentId = ParserEngine_xml_3G_LocalCell.rncNameTogeneratedIdMap.get(rncName);

        try {
            dateObject = dateFormatFromRawFileZ.parse(currentFileName.split("\\+", 2)[1].substring(1, 19));
            beginTime = dateFormatResultFiel.format(dateObject);
            isDateSetted = true;
        } catch (Exception e) {
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        currentTagValue += new String(ch, start, length);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentTagValue = "";

        switch (qName) {
            case "measInfo":
                currentSubet = new Hw3gNodeBXmlSubset();
                String functionSubsetId = attributes.getValue("measInfoId");
                currentSubet.setFunctionSubsetId(functionSubsetId);
                currentSubet.putProperty("DATA_DATE", beginTime);
                currentSubet.putProperty("RNCID", topParentId);
                currentSubet.setRncId(rncId);
                break;
            case "measCollec":
                if (isDateSetted == false) {
                    isDateSetted = true;
                    beginTime = attributes.getValue("beginTime");
                    beginTime = beginTime.split("\\+")[0].replace("T", " ");
                    try {
                        dateObject = dateFormatFromRawFile.parse(beginTime);
                        beginTime = dateFormatResultFiel.format(dateObject);
                    } catch (ParseException ex) {
                        Logger.getLogger(Hw3NodeBXmlParser.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
                break;
            case "managedElement":
                nodebName = attributes.getValue("userLabel");
                break;
            case "measValue":
                String measObjLdn = attributes.getValue("measObjLdn");
                currentSubet.setMeasObjLdn(measObjLdn);
                break;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        switch (qName) {
            case "measTypes":
                currentSubet.setFullHeader(currentTagValue.replace(" ", "|"));
                AutoCounterDefine.getInstance().setCounters(currentSubet.getFunctionSubsetId(), currentTagValue, " ");
                // current tag value is header
                break;
            case "measResults":
                currentSubet.setFullValues(currentTagValue.replace(" ", "|"));
                break;
            case "measValue":
                writeIntFiles(currentSubet);
                currentSubet.reset();
                currentSubet.putProperty("DATA_DATE", beginTime);
                currentSubet.putProperty("RNCID", topParentId);
                break;
            case "measInfo":
                currentSubet = null;
                break;
        }
    }

    @Override
    public void onstopParseOperation() {

    }

    boolean first = false;

    private void writeIntFiles(Hw3gNodeBXmlSubset subset) {
        if (rncId != null) {
            RawTableObject tableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetId(subset.getFunctionSubsetId());
            if (tableObject instanceof RawTableObject) {
                subset.parseMeasObject(tableObject);
                if (subset.isPermission() == true) {
                    String fileName = AbsParserEngine.LOCALFILEPATH + tableObject.getTableName() + AbsParserEngine.integratedFileExtension;
                    String value = CommonLibrary.get_RecordValue(subset.getFullHeader(),
                            subset.getFullValues(), tableObject.getFullColumnOrderUsingCounterNameFil("|"), "0", "|", "|");
                    value = value.replace("|NIL|", "|0|").replace("|NIL", "|0").replace("NIL|", "0|");
                    try {
                        writeIntoFilesWithController(fileName, (value + "\n"));
                    } catch (ParserIOException ex) {

                    }
                }
            }
        }
    }

}
