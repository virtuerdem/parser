/*
 * This class has a few instance parameters as
 * functionSubSet means one table in NORTHI. FunctionsubsetId It corresponds measInfoId in PM xml file.
 * counterNames correspond measTypes in xml.
 * measValue corresponds measObjLdn in xml.
 * counterValues correspond value of each counter , and measResults tag in xml.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.util.ArrayList;
import com.ttgint.parserEngine.common.RawTableObject;

public class HW2G3GCSFunctionSubSet {

    private RawTableObject tableObject;
    private String functionSubSetId;
    private String counterNames;
    private ArrayList measValues;
    private ArrayList counterValues;
    private String dataDate;
    private String neName;
    private String functionSubSetName;
    private String functionSubSetNorthiCounters = "";
    private String functionSubSetColumns = "";

    public String getFunctionSubSetColumns() {
        return functionSubSetColumns;
    }

    public void setFunctionSubSetColumns(String functionSubSetColumns) {
        this.functionSubSetColumns = functionSubSetColumns;
    }

    public HW2G3GCSFunctionSubSet() {
        measValues = new ArrayList();
        counterValues = new ArrayList();
    }

    public String getFunctionSubSetNorthiCounters() {
        return functionSubSetNorthiCounters;
    }

    public String getFunctionSubSetName() {
        return functionSubSetName;
    }

    public void setFunctionSubSetName(String functionSubSetName) {
        this.functionSubSetName = functionSubSetName;
    }

    public String getNeName() {
        return neName;
    }

    public void setNeName(String neName) {
        this.neName = neName;
    }

    public String getDataDate() {
        return dataDate;
    }

    public void setDataDate(String dataDate) {
        this.dataDate = dataDate;
    }

    public String getFunctionSubSetId() {
        return functionSubSetId;
    }

    public String getCounterNames() {
        return counterNames;
    }

    public ArrayList getMeasValue() {
        return measValues;
    }

    public ArrayList getCounterValues() {
        return counterValues;
    }

    public void setFunctionSubSetId(String functionSubSetId) {
        this.functionSubSetId = functionSubSetId;
    }

    public void setCounterNames(String counterNames) {
        this.counterNames = counterNames;
    }

    public void addMeasValue(String measValue) {
        this.measValues.add(measValue);
    }

    public void addCounterValues(String value) {
        //System.out.println("Original Value: " + value);
        if (value.contains("\"")) {
            value = killSpacesInValue(value);
        }
        //System.out.println("Updated  Value: " + value);
        this.counterValues.add(value);
    }

    // Bu kisma yeniden bakilacak, cunku baska bir ihtimalde patlar...
    private String killSpacesInValue(String val) {
        String result = "";
        String[] splittedVal = val.split("\"");
        result += splittedVal[0] + splittedVal[1].replace(" ", ".") + splittedVal[2];
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("FunctionSubsetId:").append(getFunctionSubSetId());
        sb.append("\n");
        sb.append("CounterNames    :").append(getCounterNames());
        sb.append("\n");
        sb.append("MeasValue       :").append(getMeasValue());
        sb.append("\n");
        sb.append("CounterValues   :");
        for (Object value : getCounterValues()) {
            sb.append(value.toString());
            sb.append("\n");
        }

        return sb.toString();
    }

    public RawTableObject getTableObject() {
        return tableObject;
    }

    public void setTableObject(RawTableObject tableObject) {
        this.tableObject = tableObject;
    }

    public String getTableType() {
        return tableObject.getTableType() + "-" + tableObject.getNetworkIdType();
    }

}
