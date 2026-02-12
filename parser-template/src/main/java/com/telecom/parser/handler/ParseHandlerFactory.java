package com.telecom.parser.handler;

import com.telecom.parser.handler.huawei.HwEnbPmXmlParseHandler;
import com.telecom.parser.handler.huawei.HwGnbPmXmlParseHandler;
import com.telecom.parser.handler.huawei.HwRncCmXmlParseHandler;

import java.io.File;

/**
 * Factory for creating appropriate ParseHandler
 *
 * Activity Diagram: Determine parser type (based on filename/vendor)
 *
 * Examples:
 * - HwEnbPmXmlParseHandler (Huawei 4G PM)
 * - HwGnbPmXmlParseHandler (Huawei 5G PM)
 * - HwRncCmXmlParseHandler (Huawei 3G CM)
 */
public class ParseHandlerFactory {

    /**
     * Create appropriate handler based on file and configuration
     *
     * Activity Diagram: "2. Determine parser type (filename/vendor)"
     *
     * @param xmlFile XML file to parse
     * @param vendor Vendor name (HUAWEI, ERICSSON, NOKIA, ZTE)
     * @param technology Technology (4G, 5G, 3G)
     * @param dataType Data type (PM, CM, CONF)
     * @return Appropriate ParseHandler instance
     */
    public static ParseHandler createHandler(File xmlFile, String vendor, String technology, String dataType) {

        String fileName = xmlFile.getName().toLowerCase();

        // Huawei handlers
        if ("HUAWEI".equalsIgnoreCase(vendor)) {
            return createHuaweiHandler(xmlFile, fileName, technology, dataType);
        }

        // Ericsson handlers
        else if ("ERICSSON".equalsIgnoreCase(vendor)) {
            return createEricssonHandler(xmlFile, fileName, technology, dataType);
        }

        // Nokia handlers
        else if ("NOKIA".equalsIgnoreCase(vendor)) {
            return createNokiaHandler(xmlFile, fileName, technology, dataType);
        }

        // ZTE handlers
        else if ("ZTE".equalsIgnoreCase(vendor)) {
            return createZTEHandler(xmlFile, fileName, technology, dataType);
        }

        // Default/Unknown vendor
        else {
            throw new IllegalArgumentException("Unknown vendor: " + vendor);
        }
    }

    /**
     * Create Huawei-specific handler
     */
    private static ParseHandler createHuaweiHandler(File xmlFile, String fileName, String technology, String dataType) {

        // 4G PM - eNodeB
        if (fileName.contains("enodeb") && "PM".equalsIgnoreCase(dataType)) {
            return new HwEnbPmXmlParseHandler(xmlFile);
        }

        // 5G PM - gNodeB
        else if (fileName.contains("gnodeb") && "PM".equalsIgnoreCase(dataType)) {
            return new HwGnbPmXmlParseHandler(xmlFile);
        }

        // 3G CM - RNC
        else if (fileName.contains("rnc") && "CM".equalsIgnoreCase(dataType)) {
            return new HwRncCmXmlParseHandler(xmlFile);
        }

        // 3G CM - BSC
        else if (fileName.contains("bsc") && "CM".equalsIgnoreCase(dataType)) {
            // return new HwBscCmXmlParseHandler(xmlFile);
            throw new IllegalArgumentException("BSC CM handler not implemented yet");
        }

        else {
            throw new IllegalArgumentException(
                "No Huawei handler found for: technology=" + technology + ", dataType=" + dataType + ", fileName=" + fileName
            );
        }
    }

    /**
     * Create Ericsson-specific handler
     */
    private static ParseHandler createEricssonHandler(File xmlFile, String fileName, String technology, String dataType) {
        // TODO: Implement Ericsson handlers
        throw new UnsupportedOperationException("Ericsson handlers not implemented yet");
    }

    /**
     * Create Nokia-specific handler
     */
    private static ParseHandler createNokiaHandler(File xmlFile, String fileName, String technology, String dataType) {
        // TODO: Implement Nokia handlers
        throw new UnsupportedOperationException("Nokia handlers not implemented yet");
    }

    /**
     * Create ZTE-specific handler
     */
    private static ParseHandler createZTEHandler(File xmlFile, String fileName, String technology, String dataType) {
        // TODO: Implement ZTE handlers
        throw new UnsupportedOperationException("ZTE handlers not implemented yet");
    }
}
