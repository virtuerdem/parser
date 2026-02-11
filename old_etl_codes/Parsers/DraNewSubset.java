/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.util.HashMap;

/**
 *
 * @author TurgutSimsek
 */
public class DraNewSubset {

    private String functionSubsetName;
    private String measObjLdn;
    private String variableFullHeader = "";
    private int headersize = 0;
    private int valuesize = 0;
    private String variableFullValues = "";
    private HashMap<String, String> subsetProperty;
    private boolean suspect = false;

    public void addProperty(String key, String value) {
        if (subsetProperty == null) {
            subsetProperty = new HashMap<>();
        }
        subsetProperty.put(key, value);
    }

    public void reset() {
        variableFullValues = "";
        subsetProperty = null;
        suspect = false;
    }

    public boolean isSuspect() {
        return suspect;
    }

    public void setSuspect(boolean suspect) {
        this.suspect = suspect;
    }

    public void addHeader(String header) {
        variableFullHeader = variableFullHeader + header + "|";
        headersize = headersize + 1;
    }

    public void addValue(String value) {
        variableFullValues = variableFullValues + value + "|";
        valuesize = valuesize + 1;
    }

    public String getProperty(String key) {
        return subsetProperty.get(key);
    }

    public String getFunctionSubsetName() {
        return functionSubsetName;
    }

    public String getMeasObjLdn() {
        return measObjLdn;
    }

    public void setMeasObjLdn(String measObjLdn) {
        this.measObjLdn = measObjLdn;
        addProperty("MOID", measObjLdn);
      
    }

    public String getVariableFullHeader() {
        StringBuilder st = new StringBuilder();
        st.append(variableFullHeader);
        if (subsetProperty != null) {
            for (String each : subsetProperty.keySet()) {
                st.append(each).append("|");
            }
        }

        return st.toString().substring(0, st.toString().length() - 1);
    }

    public String getVariableFullValues() {
        StringBuilder st = new StringBuilder();
        st.append(variableFullValues);
        if (valuesize != headersize) {
            for (int i = 0; i < (headersize - valuesize); i++) {
                st.append("0|");
            }
        }
        if (subsetProperty != null) {
            for (String each : subsetProperty.keySet()) {
                st.append(subsetProperty.get(each)).append("|");
            }
        }
        return st.toString().substring(0, st.toString().length() - 1);
    }

    void setFunctionSubsetName(String functionSubsetName) {
        this.functionSubsetName = functionSubsetName;
    }

}
