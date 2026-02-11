/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

/**
 *
 * @author EnesTerzi
 */
public class HWU2000SingleU2000Object {

    private String objectId = "";
    private String deviceId = "";
    private String devicename = null;
    private String resourceName = null;
    private String meadia = "";
    private String capacity = "";
    private String interfac = "";
    private String phoneCode = "0";
    private String portName = "";
    private boolean isNew = false;

    public boolean isIsNew() {
        return isNew;
    }

    public void setIsNew(boolean isNew) {
        this.isNew = isNew;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getPortName() {
        return portName;
    }

    public void setPortName(String portName) {
        this.portName = portName;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setDevicename(String devicename) {
        this.devicename = devicename;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public void setMeadia(String meadia) {
        this.meadia = meadia;
    }

    public void setInterfac(String interfac) {
        this.interfac = interfac;
    }

    public void setCapacity(String capacity) {
        this.capacity = capacity;
    }

    public void setPhoneCode(String phoneCode) {
        this.phoneCode = this.phoneCode + phoneCode;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDevicename() {
        return devicename;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getMeadia() {
        return meadia;
    }

    public String getInterfac() {
        return interfac;
    }

    public String getCapacity() {
        return capacity;
    }

    public String getPhoneCode() {
        return phoneCode;
    }

    @Override
    public String toString() {
        return objectId + "|" + deviceId
                + "|" + devicename + "|" + resourceName + "|"
                + meadia + "|" + capacity + "|" + interfac + "|" + phoneCode + "|" + portName;
    }
}
