/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import static com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_pm_Ncore.flag;
import com.ttgint.parserEngine.common.AbsParserEngine;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author enesmalik.terzi, erdi.gurbuz
 */
public class NcoreSubset {

    public int getOssNo() {
        return ossNo;
    }

    public void setOssNo(int ossNo) {
        this.ossNo = ossNo;
    }
    HashSet<String> hset = new HashSet<>();
    private String dateAsString;
    private String measType;
    private Map<String, String> propery;
    private ArrayList<String> properyAutoCounter = new ArrayList<String>();
    private List<String> measObjLdnList;
    private int ossNo = 3;

    public void createProperty() {
        propery = new LinkedHashMap<>();
    }

    public String generateId(String value) {
        BigDecimal aa = new BigDecimal(10);
        aa = aa.pow(14).multiply(BigDecimal.valueOf(ossNo)).add(new BigDecimal(value));
        return aa.toString();

    }

    private String generatageValue(String dnValue) {
        String value = dnValue;
        if (value.matches("[-+]?\\d*\\.?\\d+")) {
            value = generateId(value);
        }
        return value;
    }

    public void measObjectOperation() {
        String[] spliitedPro;
        String dnName;
        String dnValu = "";
        try {
            //<DN><![CDATA[PLMN-PLMN/OMGW-393897]]></DN>
            for (String measObjLdn : measObjLdnList) { // pes pese dn olduğunda 
                String[] spliited = measObjLdn.split("/|,");  //OMGW-401505/FPNODE-CLA-0
                Boolean networkFlag = true;
                for (String spliited1 : spliited) {
                    spliitedPro = spliited1.split("\\-"); //OMGW-401505
                    dnName = spliitedPro[0].trim();
                    if (dnName.contains("=")) {
                        dnName = dnName.split("=")[1];
                    }
                    //PLMN-PLMN/MGW-947847,SL_TYPE=PLMN-PLMN/SLN-344_0/SL_TYPE-0,SPC_TOPO=PLMN-PLMN/SNET-NA1/SPCD-14537,SIGNALLING_LINK_SETNAME=PLMN-PLMN/SLSN-BKST2
                    dnValu = spliitedPro[1].contains(",") ? spliitedPro[1].split("\\,")[0].trim() : spliitedPro[1].trim();
                    // FPNODE-CLA-0 objectleri icin
                    if (spliitedPro.length >= 3 && !spliited1.contains(",")) {
                        dnValu = "";
                        int index = 0;
                        for (String str : spliitedPro) {
                            index++;
                            if (index == 1) {
                                continue;
                            }
                            dnValu += "-" + str;
                        }
                        dnValu = dnValu.substring(1, dnValu.length());
                    }

                    //Object'in ilk kismi icin id generate edilir
                    if (networkFlag) {
                        networkFlag = false;
                        dnValu = generatageValue(spliitedPro[1].contains(",") ? spliitedPro[1].split("\\,")[0].trim() : spliitedPro[1].trim());
                        propery.put("NETWORK_ID", dnValu);
                    } else {
                        propery.put(dnName, dnValu);
                    }
                }
            }

        } catch (Exception ex) {
        }
        measObjLdnList = null;
    }

    public void setMeasObjLdn(String measObjLdn) {
        if (measObjLdn.contains("\"")) {
            measObjLdn = measObjLdn.replace("\"", "");
        }
        if (measObjLdnList == null) {
            measObjLdnList = new ArrayList<>();
        }

        measObjLdnList.add(measObjLdn);
        measObjectOperation();
    }

    public void addObject(String object) {
        propery.put("OBJECT_NAME", object);
    }

    public void addProperty(String key, String value) {
        propery.put(key, value);

        if (flag && key != null && !properyAutoCounter.contains(key)) {
            properyAutoCounter.add(key);
        }
    }

    public String getDateAsString() {
        return dateAsString;
    }

    public void setDateAsString(String dateAsString) {
        this.dateAsString = dateAsString;
    }

    public String getMeasType() {
        return measType;
    }

    public void setMeasType(String measType) {
        this.measType = measType;
    }

    public String getFullHeader() {
        StringBuilder st = new StringBuilder();
        for (String each : propery.keySet()) {
            st.append(each).append(AbsParserEngine.resultParameter);

        }

        return st.deleteCharAt(st.length() - 1).toString();
    }

    public String getFullValues() {
        StringBuilder st = new StringBuilder();
        for (String each : propery.keySet()) {
            st.append(propery.get(each)).append(AbsParserEngine.resultParameter);
        }

        return st.deleteCharAt(st.length() - 1).toString();
    }

    public ArrayList<String> getFullAutoCounters() {
        properyAutoCounter.add("NETWORK_ID");
        properyAutoCounter.add("OBJECT_NAME");
        properyAutoCounter.add("DATA_BEGIN_TIME");
        properyAutoCounter.add("DATA_DURATION");
        properyAutoCounter.add("DATA_END_TIME");
        return properyAutoCounter;
    }
}
