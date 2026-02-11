/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.common.TableWatcher;
import java.util.HashMap;

/**
 *
 * @author TTGETERZI
 */
public class EricssonEGGSNNewSubset {

    private String measObjLdn;
    private String variableFullHeader = "";
    private int headersize = 0;
    private int valuesize = 0;
    private String variableFullValues = "";
    private HashMap<String, String> subsetProperty;
    private boolean suspect = false;
    private RawTableObject rawTableObject;
    private String measInfoId;

    private void measObjectOperation(String measObject) {
        String[] spliited = measObject.split(",");

        String functionSubsetName = spliited[0].trim();
        rawTableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(functionSubsetName);

        //for wmg file
        if (rawTableObject == null && spliited.length >= 2) {
            functionSubsetName = spliited[0].trim() + "," + spliited[1].trim();
            rawTableObject = TableWatcher.getInstance().getTableObjectFromFunctionSubsetName(functionSubsetName);
        }

        if (spliited.length > 0) {
            subsetProperty = new HashMap<>();
            for (int i = 0; i < spliited.length; i++) {
                if (spliited[i].contains("=")) {
                    String[] spliitedPro = spliited[i].split("\\=");
                    subsetProperty.put(spliitedPro[0].trim(), spliitedPro[1].trim().replace("'", ""));
                }
            }
        }
    }

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

    public String getMeasObjLdn() {
        return measObjLdn;
    }

    public void setMeasObjLdn(String measObjLdn) {
        this.measObjLdn = measObjLdn;
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

    RawTableObject getRawTableObject() {
        return rawTableObject;
    }

    public String getMeasInfoId() {
        return measInfoId;
    }

    public void setMeasInfoId(String measInfoId) {
        this.measInfoId = measInfoId;
    }

    public void setMeasObjectOperation(String measObject) {
        addProperty("MOID", measObject);
        measObjectOperation(measObject);
    }

}
