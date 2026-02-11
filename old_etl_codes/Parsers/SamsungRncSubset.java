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
public class SamsungRncSubset extends SamsungCmXmlAbsSubset {

    private String cNum;
    private String cellId;
    private String status;
    private String btsNum;
    private String cellName;

    private String generatedNeid;
    private String generatedParentId;
    private String genereatedTopParentId;
    private String generatedCnumId;

    public String getGeneratedNeid() {
        return generatedNeid;
    }

    public String getGeneratedParentId() {
        return generatedParentId;
    }

    public String getGenereatedTopParentId() {
        return genereatedTopParentId;
    }

    public String getGeneratedCnumId() {
        return generatedCnumId;
    }

    public String getcNum() {
        return cNum;
    }

    public void setcNum(String cNum) {
        this.cNum = cNum;
    }

    public String getCellId() {
        return cellId;
    }

    public void setCellId(String cellId) {
        this.cellId = cellId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getBtsNum() {
        return btsNum;
    }

    public void setBtsNum(String btsNum) {
        this.btsNum = btsNum;
    }

    public void setCellName(String cellName) {
        this.cellName = cellName;
    }

    public void generateIds(String rncId) {
        generatedNeid = generatedId(Integer.parseInt(rncId), Integer.parseInt(cellId), VENDORID_3G, RanElementsInfo.CELL.getNeTypeId());
        generatedParentId = generatedId(Integer.parseInt(rncId), Integer.parseInt(btsNum), VENDORID_3G, RanElementsInfo.BTSorNB.getNeTypeId());
        genereatedTopParentId = generatedId(Integer.parseInt(rncId), Integer.parseInt(rncId), VENDORID_3G, RanElementsInfo.BSCorRNC.getNeTypeId());
        generatedCnumId = generatedId(Integer.parseInt(rncId), Integer.parseInt(cNum), VENDORID_3G, RanElementsInfo.CELL.getNeTypeId());
    }

    @Override
    public String toString() {
        return generatedNeid + "|" + generatedParentId + "|" + genereatedTopParentId + "|" + cellName+"|37";
    }

}
