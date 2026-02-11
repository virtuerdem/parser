/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_cm_xml_Samsung;
import com.ttgint.parserEngine.common.AbsParserEngine;
import com.ttgint.parserEngine.parserHandler.SyncOutputController;
import com.ttgint.parserEngine.systemProperties.RanElementsInfo;

/**
 *
 * @author TTGETERZI
 */
public class SamsungCmXmlMainSubset extends SamsungCmXmlAbsSubset {

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    private final Pattern PatternforParentId
            = Pattern.compile("\\d+");

    private String neGroup;
    private String neVersion;
    private String neId;
    private String neName;
    private String neIp;
    private String neType;
    private String gsmParent;
    private String wcdmaParent;

    public String getNeGroup() {
        return neGroup;
    }

    public void setNeGroup(String neGroup) {
        this.neGroup = neGroup;
    }

    public String getNeVersion() {
        return neVersion;
    }

    public void setNeVersion(String neVersion) {
        this.neVersion = neVersion;
    }

    public String getNeId() {
        return neId;
    }

    public void setNeId(String neId) {
        this.neId = neId;
    }

    public String getNeName() {
        return neName;
    }

    public void setNeName(String neName) {
        this.neName = neName;
    }

    public String getNeIp() {
        return neIp;
    }

    public void setNeIp(String neIp) {
        this.neIp = neIp;
    }

    public String getNeType() {
        return neType;
    }

    public void setNeType(String neType) {
        this.neType = neType;
    }

    public String getGsmParent() {
        return gsmParent;
    }

    public void setGsmParentdirect(String id) {
        this.gsmParent = id;
    }

    public void setWcdmaParentdirect(String id) {
        this.wcdmaParent = id;
    }

    public void setGsmParent(String gsmParent) {
        Matcher mat = PatternforParentId.matcher(gsmParent);
        if (mat.find()) {
            this.gsmParent = mat.group().trim();
        } else {
            this.gsmParent = gsmParent;
        }
    }

    public String getWcdmaParent() {
        return wcdmaParent;
    }

    public void setWcdmaParent(String wcdmaParent) {
        Matcher mat = PatternforParentId.matcher(wcdmaParent);
        if (mat.find()) {
            this.wcdmaParent = mat.group().trim();
        } else {
            this.wcdmaParent = wcdmaParent;
        }
    }

    public void generateId(String date, String ipAdress) {
        switch (neType) {
            case "mbs":

                if (!wcdmaParent.equals("N/A") && !gsmParent.equals("N/A")) {

                    super.networkId = generatedId(Integer.parseInt(wcdmaParent), Integer.parseInt(neId),
                            VENDORID_3G, RanElementsInfo.BTSorNB.getNeTypeId());
                    super.parentId = generatedId(Integer.parseInt(wcdmaParent), Integer.parseInt(wcdmaParent),
                            VENDORID_3G, RanElementsInfo.BSCorRNC.getNeTypeId());
                    super.topParentId = generatedId(Integer.parseInt(wcdmaParent),
                            Integer.parseInt(wcdmaParent), VENDORID_3G, RanElementsInfo.BSCorRNC.getNeTypeId());
                    writeIntoFiles("3G", date, ipAdress);

                    super.networkId = generatedId(Integer.parseInt(gsmParent), Integer.parseInt(neId),
                            VENDORID_2G, RanElementsInfo.BTSorNB.getNeTypeId());
                    super.parentId = generatedId(Integer.parseInt(gsmParent), Integer.parseInt(gsmParent),
                            VENDORID_2G, RanElementsInfo.BSCorRNC.getNeTypeId());
                    super.topParentId = generatedId(Integer.parseInt(gsmParent),
                            Integer.parseInt(gsmParent), VENDORID_2G, RanElementsInfo.BSCorRNC.getNeTypeId());
                    writeIntoFiles("2G", date, ipAdress);

                } else if (wcdmaParent.equals("N/A") == false) {

                    super.networkId = generatedId(Integer.parseInt(wcdmaParent), Integer.parseInt(neId),
                            VENDORID_3G, RanElementsInfo.BTSorNB.getNeTypeId());
                    super.parentId = generatedId(Integer.parseInt(wcdmaParent), Integer.parseInt(wcdmaParent),
                            VENDORID_3G, RanElementsInfo.BSCorRNC.getNeTypeId());
                    super.topParentId = generatedId(Integer.parseInt(wcdmaParent),
                            Integer.parseInt(wcdmaParent), VENDORID_3G, RanElementsInfo.BSCorRNC.getNeTypeId());
                    writeIntoFiles("3G", date, ipAdress);
                } else if (gsmParent.equals("N/A") == false) {

                    super.networkId = generatedId(Integer.parseInt(gsmParent), Integer.parseInt(neId),
                            VENDORID_2G, RanElementsInfo.BTSorNB.getNeTypeId());
                    super.parentId = generatedId(Integer.parseInt(gsmParent), Integer.parseInt(gsmParent),
                            VENDORID_2G, RanElementsInfo.BSCorRNC.getNeTypeId());
                    super.topParentId = generatedId(Integer.parseInt(gsmParent),
                            Integer.parseInt(gsmParent), VENDORID_2G, RanElementsInfo.BSCorRNC.getNeTypeId());
                    writeIntoFiles("2G", date, ipAdress);
                }

                break;
            case "rnc":

                super.networkId = generatedId(Integer.parseInt(neId), Integer.parseInt(neId),
                        VENDORID_3G, RanElementsInfo.BSCorRNC.getNeTypeId());
                super.parentId = generatedId(Integer.parseInt(neId), Integer.parseInt(neId),
                        VENDORID_3G, RanElementsInfo.BSCorRNC.getNeTypeId());
                super.topParentId = generatedId(Integer.parseInt(neId),
                        Integer.parseInt(neId), VENDORID_3G, RanElementsInfo.BSCorRNC.getNeTypeId());
                writeIntoFiles("3G", date, ipAdress);
                break;
            case "bsc":
                super.networkId = generatedId(Integer.parseInt(neId), Integer.parseInt(neId),
                        VENDORID_2G, RanElementsInfo.BSCorRNC.getNeTypeId());
                super.parentId = generatedId(Integer.parseInt(neId), Integer.parseInt(neId),
                        VENDORID_2G, RanElementsInfo.BSCorRNC.getNeTypeId());
                super.topParentId = generatedId(Integer.parseInt(neId),
                        Integer.parseInt(neId), VENDORID_2G, RanElementsInfo.BSCorRNC.getNeTypeId());
                writeIntoFiles("2G", date, ipAdress);
                break;

        }

    }

    public void writeIntoFiles(String Type, String date, String ipAdress) {
        //System.out.println(this);
        try {
            SyncOutputController outputController = SyncOutputController.getInstance();

            String fileName = null;
            switch (Type) {
                case "3G":
                    fileName = AbsParserEngine.LOCALFILEPATH + ParserEngine_cm_xml_Samsung.TABLE_NAME_3G + AbsParserEngine.integratedFileExtension;
                    break;
                case "2G":
                    fileName = AbsParserEngine.LOCALFILEPATH + ParserEngine_cm_xml_Samsung.TABLE_NAME_2G +  AbsParserEngine.integratedFileExtension;
                    break;
            }
            outputController.writeIntoFiles(fileName, (this.toString() + sdf.format(new Date()) + "\n"));

        } catch (Exception ex) {

        }

    }

    @Override
    public String toString() {
        int neTypee = -1;
        switch (neType) {
            case "rnc":
            case "bsc":
                neTypee = RanElementsInfo.BSCorRNC.getNeTypeId();
                break;
            case "mbs":
                neTypee = RanElementsInfo.BTSorNB.getNeTypeId();
                break;
        }
        if (neTypee == -1) {
            throw new RuntimeException("hopp");
        }
        StringBuilder st = new StringBuilder();
        st.append(networkId).append("|");
        st.append(parentId).append("|");
        st.append(topParentId).append("|");
        st.append(neName).append("|");
        st.append(neTypee).append("|");
        return st.toString();
    }

}
