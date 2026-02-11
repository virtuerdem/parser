/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ttgint.parserEngine.Northi.Vodafone.Parsers;

import com.ttgint.parserEngine.common.AbsParserEngine;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author TTGETERZI
 */
public class HWU2000NewObjectListener {

    public static final ConcurrentHashMap<String, U2000Object> objectList = new ConcurrentHashMap<>();

    public synchronized static void addObject(String deviceId, String deviceName,
            String resourceName, String portName, String resourceMedia, String resourceCapacity, String resourceInterface, String phoneCode, String purdeResource) {
        synchronized (objectList) {
            U2000Object singleObject = objectList.get(resourceName);
            if (singleObject == null) {
                singleObject
                        = new U2000Object(deviceId,
                                deviceName, resourceName, portName,
                                resourceMedia, resourceCapacity,
                                resourceInterface, phoneCode, purdeResource);
                objectList.put(resourceName, singleObject);
            } else {

            }
        }

    }

    public static class U2000Object {

        private final String deviceId;
        private final String deviceName;
        private final String resourceName;
        private final String portName;
        private final String resourceMedia;
        private final String resourceCapacity;
        private final String resourceInterface;
        private final String phoneCode;
        private final String purdeResource;

        public U2000Object(String deviceId, String deviceName, String resourceName, String portName, String resourceMedia, String resourceCapacity, String resourceInterface, String phoneCode, String purdeResource) {
            this.deviceId = deviceId;
            this.deviceName = deviceName;
            this.resourceName = resourceName;
            this.portName = portName;
            this.resourceMedia = resourceMedia;
            this.resourceCapacity = resourceCapacity;
            this.resourceInterface = resourceInterface;
            this.phoneCode = phoneCode;
            this.purdeResource = purdeResource;

        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getDeviceName() {
            return deviceName;
        }

        public String getResourceName() {
            return resourceName;
        }

        public String getPortName() {
            return portName;
        }

        public String getResourceMedia() {
            return resourceMedia;
        }

        public String getResourceCapacity() {
            return resourceCapacity;
        }

        public String getResourceInterface() {
            return resourceInterface;
        }

        public String getPhoneCode() {
            return phoneCode;
        }

        public String getPurdeResource() {
            return purdeResource;
        }

        @Override
        public String toString() {
            return deviceId + AbsParserEngine.resultParameter + deviceName + AbsParserEngine.resultParameter + resourceName + AbsParserEngine.resultParameter
                    + resourceMedia + AbsParserEngine.resultParameter + resourceCapacity + AbsParserEngine.resultParameter
                    + resourceInterface + AbsParserEngine.resultParameter + portName + AbsParserEngine.resultParameter + phoneCode
                    + AbsParserEngine.resultParameter + purdeResource;
        }

    }
}
