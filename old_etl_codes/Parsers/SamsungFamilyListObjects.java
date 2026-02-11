/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.util.ArrayList;

/**
 *
 * @author EnesTerzi
 */
public class SamsungFamilyListObjects {

    private final String familyName;
    private final String processorName;
    private final String plmnId;
    private final String tableType;
    private final int proccessorSize;
    private final String tableName;
    private final String neType;
    private boolean flagExtraWork;
    private Object[] extraWork;

    public String getNeType() {
        return neType;
    }

    public SamsungFamilyListObjects(String familyName, String processorName, String plmnId, String tableType, int proccessorSize, String tableName, String neType) {
        this.familyName = familyName;
        this.processorName = processorName;
        this.plmnId = plmnId;
        this.tableType = tableType;
        this.proccessorSize = proccessorSize;
        this.tableName = tableName;
        this.neType = neType;
    }

    public String getFamilyName() {
        return familyName;
    }

    public String getProcessorName() {
        return processorName;
    }

    public String getPlmnId() {
        return plmnId;
    }

    public String getTableType() {
        return tableType;
    }

    public int getProccessorSize() {
        return proccessorSize;
    }

    public String getTableName() {
        return tableName;
    }

    public boolean isFlagExtraWork() {
        return flagExtraWork;
    }

    public void setFlagExtraWork(boolean flagExtraWork) {
        this.flagExtraWork = flagExtraWork;
    }

    public Object[] getExtraWork() {
        return extraWork;
    }

    public void setExtraWork(ArrayList list) {
        this.extraWork = list.toArray();
    }

}
