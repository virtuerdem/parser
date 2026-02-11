/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import com.ttgint.parserEngine.common.AbsParserEngine;

/**
 *
 * @author EnesTerzi
 */
public class EricssonESPPFunctionSubSet {

    private String fileHeader = "";
    private String fileValues = "";
    private String measTypeHeader = "";
    private String meastypeValues = "";
    private String measObjLdn = "";
    private String tableHeader = null;
    private String tableName = null;
    

    public String getTableHeader() {
        return tableHeader;
    }

    public void setTableHeader(String tableHeader) {
        this.tableHeader = tableHeader;
    }

    public String getMeastypeValues() {
        return meastypeValues;
    }

    public void setMeastypeValues(String meastypeValues) {
        this.meastypeValues = meastypeValues;
    }

    public String getMeasTypeHeader() {
        return measTypeHeader;
    }

    public void setMeasTypeHeader(String measTypeHeader) {
        this.measTypeHeader = measTypeHeader;
    }

    public String getFileValues() {
        return fileValues;
    }

    public void setFileValues(String fileValues) {
        this.fileValues = fileValues;
    }

    public String getFileHeader() {
        return fileHeader;
    }

    public void setFileHeader(String fileHeader) {
        this.fileHeader = fileHeader;
    }

    public String getMeasObjLdn() {
        return measObjLdn;
    }

    public void setMeasObjLdn(String measObjLdn) {
        this.measObjLdn = measObjLdn;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        if (this.tableName == null) {
            this.tableName = tableName;
            gettableHeader();
        }
    }

    private void gettableHeader() {
        try {

            BufferedReader reader = new BufferedReader(new FileReader(new File(this.tableName + ".txt")));
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (line.length() < 10) {
                    continue;
                }
                String[] splittedLine = line.split("#");
                if (tableHeader == null) {
                    tableHeader = splittedLine[0].trim();
                } else {
                    tableHeader = tableHeader + "|" + splittedLine[0].trim();

                }

            }
            //    System.out.println(tableHeader);
            //   System.out.println("");
        } catch (FileNotFoundException ex) {
            System.out.println("Table File Not Found " + tableName);
            System.exit(1);
        } catch (IOException ex) {
            System.out.println("Table File Not Found " + tableName);
            System.exit(1);
        }

    }

}
