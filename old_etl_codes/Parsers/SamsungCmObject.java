/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

/**
 *
 * @author TTGETERZI
 */
public class SamsungCmObject {

    private final String neId;
    private final String parentId;
    private final String topParentId;
    private final String neName;
    private final String neTypeId;

    public SamsungCmObject(String neId, String parentId, String topParentId, String neName, String neTypeId) {
        this.neId = neId;
        this.parentId = parentId;
        this.topParentId = topParentId;
        this.neName = neName;
        this.neTypeId = neTypeId;
    }

    public String getNeId() {
        return neId;
    }

    public String getParentId() {
        return parentId;
    }

    public String getTopParentId() {
        return topParentId;
    }

    public String getNeName() {
        return neName;
    }

    public String getNeTypeId() {
        return neTypeId;
    }

}
