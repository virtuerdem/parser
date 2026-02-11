/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.systemProperties.RanElementsInfo;

/**
 *
 * @author TTGETERZI
 */
public class SamsungCmXml3gSubset extends SamsungCmXmlAbsSubset {

    private String userCellId;
    private String status;
    private String bsSysId;
    private String generatedCnum;

    public void setUserLabel(String UserLabel) {
        this.UserLabel = UserLabel;
    }

    public String getUserCellId() {
        return userCellId;
    }

    public void setUserCellId(String userCellId) {
        this.userCellId = userCellId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setBsSysId(String bsSysId) {
        this.bsSysId = bsSysId;
    }

    public String getGeneratedCnum() {
        return generatedCnum;
    }

    public String getNetworkId() {
        return networkId;
    }

    public void GenerateIds(String topParentId, int cNum) {

        this.generatedCnum = generatedId(Integer.parseInt(topParentId), cNum,
                VENDORID_3G, RanElementsInfo.CELL.getNeTypeId());

        super.networkId = generatedId(Integer.parseInt(topParentId), Integer.parseInt(userCellId),
                VENDORID_3G, RanElementsInfo.CELL.getNeTypeId());

        super.parentId = generatedId(Integer.parseInt(topParentId), Integer.parseInt(bsSysId),
                VENDORID_3G, RanElementsInfo.BTSorNB.getNeTypeId());

        super.topParentId = generatedId(Integer.parseInt(topParentId),
                Integer.parseInt(topParentId), VENDORID_3G, RanElementsInfo.BSCorRNC.getNeTypeId());
    }

}
