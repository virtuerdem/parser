/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import java.math.BigDecimal;

/**
 *
 * @author EnesTerzi
 */
public class SamsungCmXmlAbsSubset {

    protected final static Integer VENDORID_2G = 30;
    protected final static Integer VENDORID_3G = 31;
    protected final static Integer VENDORID_SRAN = 32;

    protected String networkId;
    protected String parentId;
    protected String topParentId;
    protected String UserLabel;

    protected  String generatedId(Integer Parent_id, Integer ne_id, Integer vendorId, Integer elementType) {
        BigDecimal bd = new BigDecimal("10");
        BigDecimal resultBigDecimal = bd.pow(21).multiply(BigDecimal.valueOf(vendorId))
                .add((bd.pow(16).multiply(BigDecimal.valueOf(elementType))))
                .add((bd.pow(8).multiply(BigDecimal.valueOf(Parent_id))))
                .add(BigDecimal.valueOf(ne_id));
        return resultBigDecimal.toString();
    }

    @Override
    public String toString() {
        StringBuilder st = new StringBuilder();
        st.append(networkId).append("|");
        st.append(parentId).append("|");
        st.append(topParentId).append("|");
        st.append(UserLabel).append("|");
        st.append("37").append("|");
        return st.toString();
    }
}

