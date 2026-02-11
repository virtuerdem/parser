/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.commonLibrary.CommonLibrary;
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
public class EricssonESSRGGsnNewParserHandler extends SaxParserHandler {

    String neName;
    String fileDate;
    Date date;
    String currentTagValue;

    boolean isDateSetted = false;
    boolean isNeNameSetted = false;
    private final SimpleDateFormat dateFormatFromXmlFile = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private EricssonEGGSNNewSubset sub = null;

    public EricssonESSRGGsnNewParserHandler(File currentFileProgress, OperationSystemEnum operationSystem, ProgressTypeEnum progType) {
        super(currentFileProgress, operationSystem, progType);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        currentTagValue = "";

        switch (qName) {
            case "measCollec":
                if (isDateSetted == false) {
                    fileDate = attributes.getValue("beginTime");
                    fileDate = fileDate.split("\\+")[0].replace("T", " ").trim();
                    isDateSetted = true;
                    try {
                        date = dateFormatFromXmlFile.parse(fileDate);
                    } catch (ParseException ex) {
                        Logger.getLogger(EricssonESSRGGsnNewParserHandler.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                break;
            case "managedElement":
                if (isNeNameSetted == false) {
                    neName = attributes.getValue("localDn");
                    neName = neName.replace("ManagedElement=", "").trim();
                    isNeNameSetted = true;
                }
                break;
            case "measValue":
                try {
                sub.setMeasObjLdn(attributes.getValue("measObjLdn").trim());
            } catch (NullPointerException e) {
                sub.setMeasObjLdn("NULLMEASVALUE");
            }
            break;
            case "measInfo":
                sub = new EricssonEGGSNNewSubset();
                try {
                    sub.setMeasInfoId(attributes.getValue("measInfoId").trim());
                } catch (NullPointerException e) {
                    sub.setMeasInfoId("NULLMEASINFO");
                }
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
            case "measType":
                sub.addHeader(currentTagValue);
                break;
            case "r":
                sub.addValue(currentTagValue);
                break;
            case "measValue":
                writeIntoFiles();
                sub.reset();
                break;
            case "measInfo":
                sub = null;
                break;
        }
    }

    private void writeIntoFiles() {

        if (sub.getMeasInfoId().equals("NULLMEASINFO")) {
            sub.setMeasObjectOperation(sub.getMeasObjLdn());
            sub.addProperty("MOID", getMOID(sub.getMeasObjLdn()));
        } else if (sub.getMeasObjLdn().contains("[") && sub.getMeasObjLdn().contains("]")) {
            String objCounters = "";
            int i = 1;
            for (String objCounter : sub.getMeasObjLdn().split("\\]")) {
                if (objCounter.split("\\[")[1].split("\\=")[0].isEmpty()) {
                    objCounters = objCounters + ",Key" + i + "=" + objCounter.split("\\[")[1].split("\\=")[1];
                } else {
                    objCounters = objCounters + "," + objCounter.split("\\[")[1];
                }
                i++;
            }
            sub.setMeasObjectOperation(sub.getMeasInfoId() + "," + objCounters);
            sub.addProperty("MOID", objCounters.substring(1));
        } else {
            sub.setMeasObjectOperation(sub.getMeasInfoId());
            sub.addProperty("MOID", getMOID(sub.getMeasInfoId()));
        }

        RawTableObject object = sub.getRawTableObject();
        if (object != null) {
            String orjinalHeader = "GGSN_NAME|DATA_DATE|GP|" + sub.getVariableFullHeader();
            String orjinalValues = neName + "|" + fileDate + "|" + "15" + "|" + sub.getVariableFullValues();
            try {

                String value = (CommonLibrary.get_RecordValue(orjinalHeader, orjinalValues, object.getFullColumnOrderUsingCounterNameFil("|"), "0", "|", "|"));

                String outputFileName = AbsParserEngine.LOCALFILEPATH + object.getTableName() + AbsParserEngine.integratedFileExtension;

                value = value.replace("NULL", "0").replace("null", "0");

                //Bazı row'lar bozuk uretiliyor uyari gelmesin diye atlaniyor
                if (!value.contains("\t")) {
                    writeIntoFilesWithController(outputFileName, value + "\n");
                }

            } catch (Exception e) {
                //   System.out.println(orjinalHeader);
                //     System.out.println(orjinalValues);
            }

        }
    }

    @Override
    public void onStartParseOperation() {
        // throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onstopParseOperation() {
        //  throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private String getMOID(String meas) {
        String moid = "";
        for (String splitted : meas.split("\\,")) {
            if (splitted.contains("=")) {
                moid = moid + "," + splitted;
            }
        }
        if (moid.length() > 0) {
            moid = moid.substring(1);
        } else {
            moid = "0";
        }
        return moid;
    }
}
