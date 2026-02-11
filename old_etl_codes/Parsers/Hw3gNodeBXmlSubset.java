/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.math.BigDecimal;
import java.util.HashMap;
import com.ttgint.parserEngine.Northi.Vodafone.Systems.ParserEngine_xml_3G_LocalCell;
import com.ttgint.parserEngine.common.RawTableObject;
import com.ttgint.parserEngine.systemProperties.RanElementsInfo;

/**
 *
 * @author TTGETERZI
 */
public class Hw3gNodeBXmlSubset {

    private String networkId;
    private String rncId;
    private String functionSubsetId;
    private String fullHeader;
    private String fullValues;
    private String measObjLdn;
    private HashMap<String, String> property
            = new HashMap<>();
    private boolean permission = true;

    public String getFunctionSubsetId() {
        return functionSubsetId;
    }

    public boolean isPermission() {
        return permission;
    }

    public void setRncId(String rncId) {
        this.rncId = rncId;
    }

    public void setFunctionSubsetId(String functionSubsetId) {
        this.functionSubsetId = functionSubsetId;
    }

    public String getFullHeader() {
        StringBuilder builder = new StringBuilder();
        if (property.isEmpty() == false) {
            for (String keyset : property.keySet()) {
                builder.append(keyset).append("|");
            }

        }
        builder.append(fullHeader);

        String returnValue = builder.toString();
        return returnValue.substring(0, returnValue.length() - 1);
    }

    public void setFullHeader(String fullHeader) {
        this.fullHeader = fullHeader;
    }

    public String getFullValues() {
        StringBuilder builder = new StringBuilder();
        if (property.isEmpty() == false) {
            for (String keyset : property.keySet()) {
                builder.append(property.get(keyset)).append("|");
            }
        }
        builder.append(fullValues);

        String returnValue = builder.toString();
        return returnValue.substring(0, returnValue.length() - 1);
    }

    public void setFullValues(String fullValues) {
        this.fullValues = fullValues;
    }

    public String getMeasObjLdn() {
        return measObjLdn;
    }

    public void setMeasObjLdn(String measObjLdn) {
        this.measObjLdn = measObjLdn;
    }

    public void putProperty(String pro, String value) {
        property.put(pro, value);
    }

    public void reset() {
        property = new HashMap<>();
        fullValues = null;
    }

    public void parseMeasObject(RawTableObject tableObject) {
        switch (tableObject.getTableType()) {
            case "NODEB":
                String NodebName = measObjLdn.split("\\/")[1].split("\\:")[1].trim();
                if (NodebName.contains("=")) {
                    NodebName = measObjLdn.split("\\/")[1].split("\\:")[1].split("=")[1].trim();
                }
                String networkdIdd = ParserEngine_xml_3G_LocalCell.nodebNameToNodeBId.get(NodebName);
                if (networkdIdd != null) {
                    property.put("NETWORK_ID", networkdIdd);
                } else {
                    permission = false;
                }
                break;
            case "BOARD":
                String nodebName = measObjLdn.split("\\/")[0];
                String boardName = measObjLdn.split("\\/")[1].split(":")[1];
                String networkIdd = ParserEngine_xml_3G_LocalCell.nodebNameToNodeBId.get(nodebName);
                if (networkIdd != null) {
                    property.put("NETWORK_ID", networkIdd);
                    property.put("BOARD_NAME", boardName);
                } else {
                    permission = false;
                }
                break;
            case "LOCALCELL":
                String nodeBName = measObjLdn.split("\\/")[0];
                String localCell;
                if (measObjLdn.contains("Function Name")) {
                    localCell = measObjLdn.split("\\/")[1].split("Local Cell ID=")[1].split("\\,")[0].trim();
                } else {
                    localCell = measObjLdn.split("\\/")[1].split("\\=")[1].trim();
                }
                String networkdId = ParserEngine_xml_3G_LocalCell.nodebNameToNodeBId.get(nodeBName);
                if (networkdId != null) {
                    property.put("NETWORK_ID", networkdId);
                    property.put("LOCAL_CELL_ID", networkIdGenerator(Integer.parseInt(rncId), Integer.parseInt(localCell), RanElementsInfo.CELL.getNeTypeId()));
                } else {
                    permission = false;
                }
                break;
        }
    }

    private String networkIdGenerator(int parentID, int childID, int elementType) {

        String result = null;
        BigDecimal bd = new BigDecimal("10");
        BigDecimal resultBigDecimal = bd.pow(21).multiply(BigDecimal.valueOf(4)).
                add(bd.pow(16).multiply(BigDecimal.valueOf(elementType))).
                add(bd.pow(8).multiply(BigDecimal.valueOf(parentID))).
                add(BigDecimal.valueOf(childID));

        return resultBigDecimal.toString();
    }
}
