/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.systemProperties.RanElementsInfo;

/**
 *
 * @author EnesTerzi
 */
public interface SamsungOutputInterface {

    void writeIntoFiles(String type, String line, RanElementsInfo ranElement);

}
