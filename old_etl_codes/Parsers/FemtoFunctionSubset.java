/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.util.HashMap;

/**
 *
 * @author EnesTerzi
 */
public class FemtoFunctionSubset {

    private String measName;
    private final HashMap<String, String> measTypeList = new HashMap<>(); 

    
    public String getMeasName() {
        return measName;
    }

    public void setMeasName(String measName) {
        this.measName = measName;
    }

    public void addMeasType(String measType) {
        measTypeList.put(measType, "");
    }

    public void addMeasValue(String measType, String measValue) {
        measTypeList.put(measType, measValue);
    }
    
    public HashMap returnCounterTypeAndValue() {
        return measTypeList;
    }
   

}
