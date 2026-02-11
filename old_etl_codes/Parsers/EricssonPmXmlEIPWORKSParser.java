package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import com.ttgint.parserEngine.common.AbsParserEngine;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class EricssonPmXmlEIPWORKSParser extends DefaultHandler implements Runnable {

    private final String rawFileName;
    private final String parsedFileName;
    private final String rawTableName;
    private final String dataDate;
    private final String neName;

    boolean gpFlag = false;
    boolean mtFlag = false;
    boolean moidFlag = false;
    boolean rFlag = false;

    String granularityPeriod;
    String moid;
    StringBuilder counterNames = new StringBuilder();
    StringBuilder counterValues = new StringBuilder();

    public EricssonPmXmlEIPWORKSParser(String rawFileName, String rawTableName) {
        this.rawFileName = rawFileName;
        this.neName = rawFileName.replace(AbsParserEngine.LOCALFILEPATH, "").split("_")[1];
        this.dataDate = rawFileName.replace(AbsParserEngine.LOCALFILEPATH, "").substring(1, 14).replace(".", "");
        this.rawTableName = rawTableName;
        this.parsedFileName = AbsParserEngine.LOCALFILEPATH + this.rawTableName + AbsParserEngine.integratedFileExtension;
    }

    @Override
    public void run() {
        parseDocument();

        try {
            java.nio.file.Files.delete(new File(rawFileName).toPath());
        } catch (IOException ex) {
            Logger.getLogger(EricssonPmXmlEIPWORKSParser.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void parseDocument() {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(rawFileName, this);

            String splittedmoid[] = moid.split(",");
            for (String eachItem : splittedmoid) {
                this.counterNames.append(eachItem.split("=")[0]).append("|");
                this.counterValues.append(eachItem.split("=")[1]).append("|");
            }
            this.counterNames.append("granularity_period").append("|").append("DATA_DATE");
            this.counterValues.append(this.granularityPeriod).append("|").append(dataDate);

            try (FileOutputStream out = new FileOutputStream(parsedFileName, true)) {
                /* Yeni sisteme gore bu eski sistem tekrar 
                    aktif olursa implemente edilecek 
                */
                //out.write(counterNames.toString().getBytes());
                //String tableColumns = CommonLibrary.get_NorthiTableColumnsForGetValue(this.rawTableName, null);
                //String recordAfterGetVal = CommonLibrary.get_RecordValue(counterNames.toString(), counterValues.toString(), tableColumns, "0", "|", "|");
                //out.write((recordAfterGetVal + "\n").getBytes());
                out.close();
            }

        } catch (ParserConfigurationException | SAXException | IOException ex) {
//            ex.printStackTrace();
        }
    }

    @Override
    public void startElement(String s, String s1, String elementName, Attributes attributes) {

        if (elementName.equalsIgnoreCase("gp")) {
            gpFlag = true;
        }

        if (elementName.equalsIgnoreCase("mt")) {
            mtFlag = true;
        }

        if (elementName.equalsIgnoreCase("moid")) {
            moidFlag = true;
        }

        if (elementName.equalsIgnoreCase("r")) {
            rFlag = true;
        }
    }

    @Override
    public void characters(char[] ac, int start, int length) {

        if (gpFlag) {
            granularityPeriod = new String(ac, start, length);
            gpFlag = false;
        }

        if (mtFlag) {
            String counterName = new String(ac, start, length);
            counterNames.append(counterName.replace("ipworksDnsServ", "")).append("|");
            mtFlag = false;
        }

        if (moidFlag) {
            moid = new String(ac, start, length);
            moidFlag = false;
        }

        if (rFlag) {
            counterValues.append(new String(ac, start, length)).append("|");
            rFlag = false;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) {
    }

}
