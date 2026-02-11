/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

/**
 *
 * @author EnesTerzi
 */
public class SamsungCmObjects {
    
    private final String neName;
    private final String neId;
    private final String parentId;

    public SamsungCmObjects(String neName, String neId, String parentId) {
        this.neName = neName;
        this.neId = neId;
        this.parentId = parentId;
    }

    public String getNeName() {
        return neName;
    }

    public String getNeId() {
        return neId;
    }

    public String getParentId() {
        return parentId;
    }
    
    
    
    
    
}
