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
public class SamsungBscSubset extends SamsungCmXmlAbsSubset {

    private String cNum;
    private String ValidFlag;
    private String BtsId;
    private String Cellidx;
    private String UserLabel;
    private String generatedNeid;
    private String generatedParentId;
    private String genereatedTopParentId;

    public String getcNum() {
        return cNum;
    }

    public void setcNum(String cNum) {
        this.cNum = cNum;
    }

    public String getValidFlag() {
        return ValidFlag;
    }

    public void setValidFlag(String ValidFlag) {
        this.ValidFlag = ValidFlag;
    }

    public String getBtsId() {
        return BtsId;
    }

    public void setBtsId(String BtsId) {
        this.BtsId = BtsId;
    }

    public String getCellidx() {
        return Cellidx;
    }

    public void setCellidx(String Cellidx) {
        this.Cellidx = Cellidx;
    }

    public String getUserLabel() {
        return UserLabel;
    }

    public void setUserLabel(String UserLabel) {
        this.UserLabel = UserLabel;
    }

    public void generateIds(String bscId) {
        generatedNeid = generatedId(Integer.parseInt(BtsId), Integer.parseInt(Cellidx), VENDORID_2G, RanElementsInfo.CELL.getNeTypeId());
        generatedParentId = generatedId(Integer.parseInt(bscId), Integer.parseInt(BtsId), VENDORID_2G, RanElementsInfo.BTSorNB.getNeTypeId());
        genereatedTopParentId = generatedId(Integer.parseInt(bscId), Integer.parseInt(bscId), VENDORID_2G, RanElementsInfo.BSCorRNC.getNeTypeId());
    }

    @Override
    public String toString() {
        return generatedNeid + "|" + UserLabel + "|" + generatedParentId + "|" + genereatedTopParentId;
    }

}
