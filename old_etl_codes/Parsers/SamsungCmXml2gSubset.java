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
public class SamsungCmXml2gSubset extends SamsungCmXmlAbsSubset {

    private String userCellId;
    private String ValidFlag;
    private String bsSysId;
  

    public void setUserCellId(String userCellId) {
        this.userCellId = userCellId;
    }

    public String getValidFlag() {
        return ValidFlag;
    }

    public void setValidFlag(String ValidFlag) {
        this.ValidFlag = ValidFlag;
    }



    public void setBsSysId(String bsSysId) {
        this.bsSysId = bsSysId;
    }


    public void setUserLabel(String UserLabel) {
        this.UserLabel = UserLabel;
    }

    public void GenerateIds(String topParentId) {
        this.networkId = generatedId(Integer.parseInt(topParentId), Integer.parseInt(userCellId),
                VENDORID_2G, RanElementsInfo.CELL.getNeTypeId());

        this.parentId = generatedId(Integer.parseInt(topParentId), Integer.parseInt(bsSysId),
                VENDORID_2G, RanElementsInfo.BTSorNB.getNeTypeId());

        this.topParentId = generatedId(Integer.parseInt(topParentId),
                Integer.parseInt(topParentId), VENDORID_2G, RanElementsInfo.BSCorRNC.getNeTypeId());
    }

  

}
