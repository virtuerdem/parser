/*
 * To change this license fullHeader, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

/**
 *
 * @author Administrator
 */
public class PcrfPmXmlParserSubset {

    String fullHeader="";
    String fullValues="";
    String functionSubsetName;

    public String getFullHeader() {
        return fullHeader;
    }

    public String getFullValues() {
        return fullValues.substring(0,fullValues.length()-1);
    }

    public String getFunctionSubsetName() {
      //  System.out.println("func name " + functionSubsetName);
        return functionSubsetName;
    }

    public void setFunctionSubsetName(String functionSubsetName) {
        this.functionSubsetName = functionSubsetName;
    }

    public void addHeader(String header) {
       fullHeader += header + "|";
    }

    public void addValues(String values) {
            fullValues+= values+"|";
    }

    void reset() {
      this.fullHeader = "";
      this.fullValues="";
    }

}
