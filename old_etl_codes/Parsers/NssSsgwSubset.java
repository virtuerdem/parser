/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.math.BigDecimal;
import java.util.HashMap;

/**
 *
 * @author TTGETERZI
 */
public class NssSsgwSubset {

    private String tableName;
    private String dataDate;
    private String functionSubsetName;
    private String measObjLdn;
    private String variableFullHeader = "";
    private int headersize = 0;
    private int valuesize = 0;
    private String variableFullValues = "";
    private HashMap<String, String> subsetProperty = new HashMap<>();
    private int ossNo;

    public int getOssNo() {
        return ossNo;
    }

    public void setOssNo(int ossNo) {
        this.ossNo = ossNo;
    }

    private void measObjectOperation() {
        try {
            String[] spliited = measObjLdn.split("/");

            for (String spliited1 : spliited) {
                String[] spliitedPro = spliited1.split("\\-");
                String dnName = spliitedPro[0].trim();
                String dnValu = generatageValue(dnName, spliitedPro[1].trim());
                if (dnName.equals("OMGW"))
                    dnName = "MGW";
                subsetProperty.put(dnName, dnValu);
            }
        } catch (Exception ex) {
            //SSGW do nothing;
        }
    }

    public void setFunctionSubset(String sub) {
        this.functionSubsetName = sub;
    }

    public void reset() {
        variableFullValues = "";
        subsetProperty = null;
    }
    
    public void resetValues() {
        variableFullValues = "";
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
        if (measObjLdn.contains("\"")) {
            measObjLdn = measObjLdn.replace("\"", "");
        }
        this.measObjLdn = measObjLdn;
        measObjectOperation();
    }

    public String getVariableFullHeader() {
        StringBuilder st = new StringBuilder();
        st.append(variableFullHeader);
        if (subsetProperty.isEmpty() == false) {
            for (String each : subsetProperty.keySet()) {
                st.append(each).append("|");
            }
        }

        return st.toString().substring(0, st.toString().length() - 1);
    }

    public String getVariableFullValues() {
        StringBuilder st = new StringBuilder();
        st.append(variableFullValues);
        if (subsetProperty.isEmpty() == false) {
            for (String each : subsetProperty.keySet()) {
                st.append(subsetProperty.get(each)).append("|");
            }
        }

        return st.toString().substring(0, st.toString().length() - 1);
    }

    private String generatageValue(String dnType, String dnValue) {
        String value = dnValue;
        switch (dnType) {
            case "MGW":
            case "MSC":
            case "HLR":
            case "SRR":
            case "SCSN":
            case "OMGW":
                value = generateId(value);
                break;
        }
        return value;
    }

    public String generateId(String value) {
        BigDecimal aa = new BigDecimal(10);
        aa = aa.pow(14).multiply(BigDecimal.valueOf(ossNo)).add(new BigDecimal(value));

        return aa.toString();

    }

}
