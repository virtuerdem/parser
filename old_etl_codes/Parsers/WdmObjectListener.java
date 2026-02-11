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
 * @author turgut.simsek
 */
public class WdmObjectListener {

    public static final ConcurrentHashMap<String, WdmObjectListener.WdmObject> objectList = new ConcurrentHashMap<>();

    public synchronized static void addObject(String deviceId, String deviceName,
            String resourceName, String neName, String shelfName, String slotId, String boardName, String portId) {
        synchronized (objectList) {
            WdmObjectListener.WdmObject singleObject = objectList.get(resourceName);
            if (singleObject == null) {
                singleObject
                        = new WdmObjectListener.WdmObject(deviceId,
                                deviceName, resourceName, neName, shelfName, slotId, boardName, portId);
                objectList.put(resourceName, singleObject);
            } else {

            }
        }

    }

    public static class WdmObject {

        private final String deviceId;
        private final String deviceName;
        private final String resourceName;
        private String neName;
        private String shelfName;
        private String slotId;
        private String boardName;
        private String portId;

        private WdmObject(String deviceId, String deviceName, String resourceName, String neName, String shelfName, String slotId, String boardName, String portId) {

            this.deviceId = deviceId;
            this.deviceName = deviceName;
            this.resourceName = resourceName;

            this.neName = neName;
            this.shelfName = shelfName;
            this.slotId = slotId;
            this.boardName = boardName;
            this.portId = portId;

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

        @Override
        public String toString() {
            String rs = AbsParserEngine.resultParameter;

            return  deviceId + rs + deviceName  + rs + neName + rs + shelfName + rs + slotId + rs + boardName + rs + portId +rs +resourceName;
        }

    }

}
