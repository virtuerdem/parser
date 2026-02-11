/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.common.AbsParserEngine;
import java.util.ArrayList;

/**
 *
 * @author turgut.simsek
 */
public class Ulak4gSubset {

    private String measObject = "";
    private String functionSubsetName = "";
    private String counterValues = "";
    private String fileHeaderNames = "";
    private String neName = "";
    private String dataDate;

    private final ArrayList<String> counterNameFileList;
    private ArrayList<String> counterValueList;

    public Ulak4gSubset() {
        counterNameFileList = new ArrayList<>();
        counterValueList = new ArrayList<>();
    }

    public String getCounterNames() {
        String counterNames = "";

        for (String counterName : counterNameFileList) {
            counterNames += counterName + AbsParserEngine.resultParameter;
        }

        counterNames = counterNames.substring(0, counterNames.length() - 1);
        return counterNames;
    }

    public String getCounterValues() {

        for (String counterValue : counterValueList) {
            counterValues += counterValue + AbsParserEngine.resultParameter;
        }

        return counterValues;
    }

    public ArrayList<String> getCounterValueList() {
        return counterValueList;
    }

    public String getFileHeaderNames() {

        if (fileHeaderNames.equals("")) {

            for (String counterName : counterNameFileList) {
                fileHeaderNames += counterName + AbsParserEngine.resultParameter;
            }
        }
        return fileHeaderNames;
    }

    public void setCounterValueList(ArrayList<String> counterValueList) {
        this.counterValueList = counterValueList;
    }

    public void setCounterValues(String counterValues) {
        this.counterValues = counterValues;
    }

    public void setMeasObject(String measObject) {
        this.measObject = measObject;   //<measValue measObjLdn="ManagedElement=ULAK-2,ENodeBFunction=59125,EUtranCellFDD=cell-0">
        String[] objectArray = measObject.split("\\,");
        this.neName = objectArray[0].split("\\=")[1]; // ULAK-2
    }

    public String getFunctionSubsetName() {
        if (functionSubsetName.isEmpty()) {
            for (String element : measObject.split("\\,")) {
                String elementKey = element.split("\\=")[0];
                if (!elementKey.equals("ManagedElement")) {
                    functionSubsetName += elementKey + "|";
                }
            }
            functionSubsetName = functionSubsetName.substring(0, functionSubsetName.length() - 1);
        }
        return functionSubsetName;
    }

    public String getMeasObject() {
        return measObject;
    }

    public String getNeName() {
        return neName;
    }

    public void addCounterNameList(String measValue) {
        this.counterNameFileList.add(measValue);
    }

    public void addCounterValueList(String value) {
        this.counterValueList.add(value);
    }

    public String getDataDate() {
        return dataDate;
    }

    public void setDataDate(String dataDate) {
        this.dataDate = dataDate;
    }

}
