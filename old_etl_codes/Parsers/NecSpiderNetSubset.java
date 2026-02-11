/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.common.RawCounterObject;
import com.ttgint.parserEngine.common.RawTableObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 * @author turgut.simsek
 */
public class NecSpiderNetSubset {

    private RawTableObject tableObject;
    private String dataDate;
    private final LinkedHashMap<Integer, String> counterIndexNameMap;
    private final LinkedHashMap<Integer, String> counterIndexValueMap;
    private final LinkedHashMap<String, String> fileHeaderValueMap;
    private LinkedHashMap<String, List<RawCounterObject>> counterNameFileRawObjListMap;

    private String fileHeaderNames = "";
    private String counterValues = "";
    private String measObject = "";

    public NecSpiderNetSubset() {
        counterIndexNameMap = new LinkedHashMap<>();
        counterIndexValueMap = new LinkedHashMap<>();
        fileHeaderValueMap = new LinkedHashMap<>();
    }

    public String getDataDate() {
        return dataDate;
    }

    public void setDataDate(String dataDate) {
        this.dataDate = dataDate;
    }

    public void setMeasObject(String measObject) {
        this.measObject = measObject;
    }

    public String getMeasObject() {
        return measObject;
    }

    public void setTableObject(RawTableObject tableObject) {
        this.tableObject = tableObject;
        if (tableObject != null) {
            fillCounterNameLookUpRawObjListMap();
        }

    }

    public RawTableObject getTableObject() {
        return tableObject;
    }

    public HashMap<String, String> getFileHeaderValueMap() {
        return fileHeaderValueMap;
    }

    public HashMap<Integer, String> getCounterIndexNameMap() {
        return counterIndexNameMap;
    }

    private void fillCounterNameLookUpRawObjListMap() {

        counterNameFileRawObjListMap = new LinkedHashMap<>();

        for (RawCounterObject rawCounterObject : tableObject.getCounterObjectList()) {

            String counterNameFile = rawCounterObject.getCounterNameFile();

            if (counterNameFileRawObjListMap.containsKey(counterNameFile)) {
                counterNameFileRawObjListMap.get(counterNameFile).add(rawCounterObject);
            } else {

                List<RawCounterObject> rawCounterObjList = new ArrayList<>();
                rawCounterObjList.add(rawCounterObject);

                counterNameFileRawObjListMap.put(counterNameFile, rawCounterObjList);
            }
        }
    }

    public void addCounterIndexNameMap(int index, String counterName) {
        counterIndexNameMap.put(index, counterName);
    }

    public void addCounterIndexValueMap(int index, String counterValue) {
        counterIndexValueMap.put(index, counterValue);
    }

    public String getFileHeaderNames(String joinChar) {

        for (Integer key : counterIndexNameMap.keySet()) {
            String counterName = counterIndexNameMap.get(key);

            if (counterNameFileRawObjListMap.containsKey(counterName)) {

                List<RawCounterObject> list = counterNameFileRawObjListMap.get(counterName); // counterName   A  CounterNameLookup A/0 
                //                A                    A/1                     
                //                 A                    A/2 
                for (RawCounterObject rawCounterObject : list) {
                    fileHeaderValueMap.put(rawCounterObject.getCounterNameLookup(), "0");
                }
            }
        }

        for (String counterNameLookup : fileHeaderValueMap.keySet()) {
            fileHeaderNames += counterNameLookup + joinChar;
        }
        fileHeaderNames = fileHeaderNames.substring(0, fileHeaderNames.length() - 1);
        return fileHeaderNames;
    }

    public String getCounterValues(String joinChar) {

        for (Integer key : counterIndexValueMap.keySet()) {

            String values = counterIndexValueMap.get(key);
            values = values.replace("[", "");  // values [ 0 0 0 0 ]
            values = values.replace("]", "");
            values = values.trim();
            List<String> valueList = Arrays.asList(values.split("\\ "));

            String counterNameFile = counterIndexNameMap.get(key);
            List<RawCounterObject> rawCounterObjectList = counterNameFileRawObjListMap.get(counterNameFile);// counterName   A  CounterNameLookup A/0 
                                                                                                           //                A                    A/1                     
                                                                                                          //                 A                    A/2 
                                                                                                         //  System.out.println("get Counter Values : " + valueList);
            if (rawCounterObjectList != null) {
                for (RawCounterObject rawCounterObject : rawCounterObjectList) {

                    String counterNameLookUp = rawCounterObject.getCounterNameLookup(); // A/0 ,A/1 , A/2

                    if (counterNameLookUp != null && fileHeaderValueMap.containsKey(counterNameLookUp)) {

                        if (counterNameLookUp.contains("/")) {
                            int counterValueIndex = Integer.parseInt(counterNameLookUp.split("\\/")[1])-1;
                            fileHeaderValueMap.put(counterNameLookUp, valueList.get(counterValueIndex));
                        } else {
                            fileHeaderValueMap.put(counterNameLookUp, valueList.get(0));
                        }
                    }
                }
            }
        }

        for (String value : fileHeaderValueMap.values()) {
            counterValues += value + joinChar;
        }

        counterValues = counterValues.substring(0, counterValues.length() - 1);

        return counterValues;
    }

    public void resetCounterValues() {

        for (Integer counterIndex : counterIndexValueMap.keySet()) {
            counterIndexValueMap.put(counterIndex, "0");
        }

        for (String counterNameLookup : fileHeaderValueMap.keySet()) {
            fileHeaderValueMap.put(counterNameLookup, "0");
        }

        counterValues = "";
        fileHeaderNames  = "";

    }

}
