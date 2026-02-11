/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.util.ArrayList;
import java.util.HashMap;
import com.ttgint.parserEngine.common.RawCounterObject;

/**
 *
 * @author TTGETERZI
 */
public class SamsungPmSubset_v2 {

    private String networkId;
    private String initDate;
    public final HashMap<RawCounterObject, Double> counterValue = new HashMap<>();
    private final HashMap<String, String> property = new HashMap<>();
    private final ArrayList<String> header;
    private int counterAddSizeTotal = 0;

    public void incareseSize() {
        counterAddSizeTotal++;
    }

    public SamsungPmSubset_v2(ArrayList<String> header) {
        this.header = header;
    }

    public String getNetworkId() {

        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public String getInitDate() {
        return initDate;
    }

    public void setInitDate(String initDate) {
        this.initDate = initDate;
    }

    public void putCounters(RawCounterObject counterObject, Double Value) {
        // System.out.println(Value);
        counterValue.put(counterObject, Value);
    }

    public void aggValue(RawCounterObject counterObject, Double Value) {
        Double intbeforeValue = counterValue.get(counterObject);
        switch (counterObject.getCounterAgg()) {
            case "SUM":
                intbeforeValue = intbeforeValue + Value;
                break;
            case "MIN":
                if (Value < intbeforeValue) {
                    intbeforeValue = Value;
                }
                break;
            case "MAX":
                if (Value > intbeforeValue) {
                    intbeforeValue = Value;
                }
                break;
            case "AVG":
                intbeforeValue = intbeforeValue * counterAddSizeTotal;
                intbeforeValue = intbeforeValue + Value;
                intbeforeValue = intbeforeValue / (counterAddSizeTotal + 1);
                break;
        }
        counterValue.remove(counterObject);
        counterValue.put(counterObject, intbeforeValue);
    }

    public void putProperty(String propName, String propValue) {
        property.put(propName, propValue);
    }

    public String getHeader() {
        StringBuilder st = new StringBuilder();
        for (String each : property.keySet()) {
            st.append(each).append("|");
        }
        for (RawCounterObject each : counterValue.keySet()) {
            st.append(each.getCounterNameFile()).append("|");
        }

        return st.toString().substring(0, st.toString().length() - 1);
    }

    public String getValue() {
        StringBuilder st = new StringBuilder();
        for (String each : property.keySet()) {
            st.append(property.get(each)).append("|");
        }
        for (RawCounterObject each : counterValue.keySet()) {
            st.append(counterValue.get(each)).append("|");
        }

        return st.toString().substring(0, st.toString().length() - 1);
    }

}
