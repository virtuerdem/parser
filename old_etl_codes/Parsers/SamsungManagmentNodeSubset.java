/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.systemProperties.RanElementsInfo;

/**
 *
 * @author EnesTerzi
 */
public class SamsungManagmentNodeSubset extends SamsungCmXmlAbsSubset {

    private String neGroup;
    private String neVersion;
    private String neId;
    private String neName;
    private String neIp;
    private String neType;
    private String gsmParent;
    private String wcdmaParent;
    private String generatedneId;
    private String genaratedgsmParent;
    private String generatedwcdmaParent;
    private String gsmTopParentId;
    private String wcdmaTopParentId;
    private String OrjinalNeIdFromSamsung;
    private String bscId;
    private String rncId;

    public String getOrjinalNeIdFromSamsung() {
        return OrjinalNeIdFromSamsung;
    }

    public void setOrjinalNeIdFromSamsung(String OrjinalNeIdFromSamsung) {
        this.OrjinalNeIdFromSamsung = OrjinalNeIdFromSamsung;
    }

    public String getGsmTopParentId() {
        return gsmTopParentId;
    }

    public String getWcdmaTopParentId() {
        return wcdmaTopParentId;
    }

    public String getGeneratedneId() {
        return generatedneId;
    }

    public String getGenaratedgsmParent() {
        return genaratedgsmParent;
    }

    public String getGeneratedwcdmaParent() {
        return generatedwcdmaParent;
    }

    public String getNeVersion() {
        return neVersion;
    }

    public void setNeVersion(String neVersion) {
        this.neVersion = neVersion;
    }

    public String getNeGroup() {
        return neGroup;
    }

    public void setNeGroup(String neGroup) {
        this.neGroup = neGroup;
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

    public void setGsmParent(String gsmParent) {
        this.gsmParent = gsmParent;
    }

    public String getWcdmaParent() {
        return wcdmaParent;
    }

    public void setWcdmaParent(String wcdmaParent) {
        this.wcdmaParent = wcdmaParent;
    }

    public void generateIds() {

        switch (neGroup) {
            case "2G_Vodafone":
                switch (neType) {
                    case "bsc":
                        generatedneId = generatedId(Integer.parseInt(neId), Integer.parseInt(neId), VENDORID_2G, RanElementsInfo.BSCorRNC.getNeTypeId());
                        break;
                    case "mbs":
                        generatedneId = generatedId(Integer.parseInt(bscId), Integer.parseInt(neId), VENDORID_2G, RanElementsInfo.BTSorNB.getNeTypeId());
                        gsmTopParentId = generatedId(Integer.parseInt(bscId), Integer.parseInt(bscId), VENDORID_2G, RanElementsInfo.BSCorRNC.getNeTypeId());
                        genaratedgsmParent = generatedneId;
                        break;
                }

                break;

            case "3G_Vodafone":
                switch (neType) {
                    case "rnc":
                        generatedneId = generatedId(Integer.parseInt(neId), Integer.parseInt(neId), VENDORID_3G, RanElementsInfo.BSCorRNC.getNeTypeId());
                        break;
                    case "mbs":
                        generatedneId = generatedId(Integer.parseInt(rncId), Integer.parseInt(neId), VENDORID_3G, RanElementsInfo.BTSorNB.getNeTypeId());
                        wcdmaTopParentId = generatedId(Integer.parseInt(rncId), Integer.parseInt(rncId), VENDORID_3G, RanElementsInfo.BSCorRNC.getNeTypeId());
                        generatedwcdmaParent = generatedneId;
                        break;
                }
                break;

            case "S-RAN":
                try {
                    if (!rncId.equals("N/A")) {
                        generatedwcdmaParent = generatedId(Integer.parseInt(rncId), Integer.parseInt(neId), VENDORID_3G, RanElementsInfo.BTSorNB.getNeTypeId());
                        wcdmaTopParentId = generatedId(Integer.parseInt(rncId), Integer.parseInt(rncId), VENDORID_3G, RanElementsInfo.BSCorRNC.getNeTypeId());

                    }
                    if (!bscId.equals("N/A")) {
                        genaratedgsmParent = generatedId(Integer.parseInt(bscId), Integer.parseInt(neId), VENDORID_2G, RanElementsInfo.BTSorNB.getNeTypeId());
                        gsmTopParentId = generatedId(Integer.parseInt(bscId), Integer.parseInt(bscId), VENDORID_2G, RanElementsInfo.BSCorRNC.getNeTypeId());
                    }

                    generatedneId = generatedId(Integer.parseInt(neId), Integer.parseInt(neId), VENDORID_SRAN, RanElementsInfo.BTSorNB.getNeTypeId());
                } catch (Exception e) {

                }
                break;

            default:

                break;
        }

    }

    @Override
    public String toString() {
        return neGroup + "|" + neId + "|" + neName + "|" + neIp + "|" + neType + "|" + generatedneId + "|" + genaratedgsmParent + "|" + generatedwcdmaParent + "|" + OrjinalNeIdFromSamsung;
    }

    public String getBscId() {
        return bscId;
    }

    public void setBscId(String bscId) {
        this.bscId = bscId;
    }

    public String getRncId() {
        return rncId;
    }

    public void setRncId(String rncId) {
        this.rncId = rncId;
    }

}
