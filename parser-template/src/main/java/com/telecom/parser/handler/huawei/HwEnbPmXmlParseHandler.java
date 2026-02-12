package com.telecom.parser.handler.huawei;

import com.telecom.parser.handler.ParseHandler;
import org.xml.sax.Attributes;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * Huawei eNodeB PM XML Parse Handler
 *
 * Parses Huawei 4G LTE eNodeB Performance Management XML files
 *
 * Example XML structure:
 * <measData>
 *   <measInfo>
 *     <measType>RSRP</measType>
 *     <measType>Throughput</measType>
 *     <measValue>
 *       <measObjLdn>eNodeB001/Cell1</measObjLdn>
 *       <r>-80</r>  <!-- RSRP value -->
 *       <r>1500</r> <!-- Throughput value -->
 *     </measValue>
 *   </measInfo>
 * </measData>
 */
public class HwEnbPmXmlParseHandler extends ParseHandler {

    // Current parsing context
    private Map<Integer, String> measTypes; // Index -> Metric name
    private Map<Integer, String> measResults; // Index -> Metric value
    private String measObjLdn; // Measurement object (Cell/eNodeB)
    private int currentMeasIndex;

    public HwEnbPmXmlParseHandler(File xmlFile) {
        super(xmlFile);
        this.measTypes = new HashMap<>();
        this.measResults = new HashMap<>();
        this.currentMeasIndex = 0;
    }

    // ==================== Filename Parsing ====================

    /**
     * Extract fragment date from Huawei filename
     * Example: "20260201_150000_eNodeB_001_PM.xml" -> "20260201150000"
     */
    @Override
    protected String extractFragmentDate(String fileName) {
        // Huawei format: YYYYMMDD_HHMMSS_...
        if (fileName.contains("_")) {
            String[] parts = fileName.split("_");
            if (parts.length >= 2) {
                return parts[0] + parts[1]; // Concatenate date and time
            }
        }
        return "UNKNOWN";
    }

    /**
     * Extract node name from Huawei filename
     * Example: "20260201_150000_eNodeB_001_PM.xml" -> "eNodeB_001"
     */
    @Override
    protected String extractNodeName(String fileName) {
        // Huawei format: ..._eNodeB_XXX_...
        if (fileName.contains("eNodeB")) {
            int start = fileName.indexOf("eNodeB");
            int end = fileName.indexOf("_PM");
            if (end == -1) end = fileName.indexOf(".xml");
            return fileName.substring(start, end);
        }
        return "UNKNOWN";
    }

    /**
     * Extract file ID from Huawei filename
     */
    @Override
    protected String extractFileId(String fileName) {
        return fileName.replace(".xml", "");
    }

    // ==================== MeasInfo Section ====================

    /**
     * Activity Diagram: Read measInfo section (start)
     */
    @Override
    protected void startMeasInfo(Attributes attributes) {
        System.out.println("    [MeasInfo] Started new measInfo section");
        measTypes.clear();
        currentMeasIndex = 0;
    }

    /**
     * Activity Diagram: Read measInfo section (end)
     */
    @Override
    protected void endMeasInfo() {
        System.out.println("    [MeasInfo] Completed measInfo section with " + measTypes.size() + " metric types");
    }

    // ==================== MeasValue Record ====================

    /**
     * Activity Diagram: Read measValue record (start)
     */
    @Override
    protected void startMeasValue(Attributes attributes) {
        System.out.println("      [MeasValue] Started new measValue record");
        measResults.clear();
        measObjLdn = null;
        currentMeasIndex = 0;
    }

    /**
     * Activity Diagram: Read measValue record (end)
     *
     * This is where the main processing happens:
     * 1. Extract metrics (RSRP, Throughput, etc.)
     * 2. Map to table columns
     * 3. Write to CSV buffer
     * 4. Auto counter (if enabled)
     */
    @Override
    protected void endMeasValue() {
        System.out.println("      [MeasValue] Processing measValue record for: " + measObjLdn);

        // Activity Diagram: Map to table columns
        String csvRow = mapToTableColumns();

        // Activity Diagram: Write to CSV buffer
        if (csvRow != null) {
            writeToCsv("hw_enb_pm_metrics", csvRow);
            System.out.println("        [CSV] Written to CSV buffer");
        }

        // Activity Diagram: Auto counter enabled? -> Collect counter definitions
        if (autoCounterEnabled) {
            collectCounterDefinitions();
        }
    }

    // ==================== Metric Processing ====================

    /**
     * Activity Diagram: Extract metrics (RSRP, Throughput, etc.)
     */
    @Override
    protected void processMetric(String metricName, String metricValue) {

        // measType elements define the metric names
        if ("measType".equals(metricName)) {
            measTypes.put(measTypes.size(), metricValue);
            System.out.println("        [Metric Type] Index " + measTypes.size() + ": " + metricValue);
        }

        // measObjLdn identifies the object (Cell/eNodeB)
        else if ("measObjLdn".equals(metricName)) {
            this.measObjLdn = metricValue;
            System.out.println("        [Object] " + metricValue);
        }

        // r elements contain the actual metric values
        else if ("r".equals(metricName)) {
            measResults.put(currentMeasIndex, metricValue);
            String metricTypeName = measTypes.get(currentMeasIndex);
            System.out.println("        [Metric Value] " + metricTypeName + " = " + metricValue);
            currentMeasIndex++;
        }
    }

    /**
     * Check if element is a metric element
     */
    @Override
    protected boolean isMetricElement(String qName) {
        return "measType".equals(qName) || "measObjLdn".equals(qName) || "r".equals(qName);
    }

    // ==================== CSV Mapping ====================

    /**
     * Activity Diagram: Map to table columns
     *
     * Map extracted metrics to database table columns
     * Generate CSV row
     */
    private String mapToTableColumns() {
        if (measObjLdn == null || measResults.isEmpty()) {
            return null;
        }

        StringBuilder csv = new StringBuilder();

        // Common columns
        csv.append(fragmentDate).append(",");     // timestamp
        csv.append(nodeId != null ? nodeId : "").append(","); // node_id
        csv.append(measObjLdn).append(",");       // cell_identifier

        // Metric columns (map each metric type to its value)
        for (int i = 0; i < measTypes.size(); i++) {
            String metricName = measTypes.get(i);
            String metricValue = measResults.getOrDefault(i, "");

            // Sanitize value
            metricValue = metricValue.isEmpty() ? "NULL" : metricValue;

            csv.append(metricValue);

            if (i < measTypes.size() - 1) {
                csv.append(",");
            }
        }

        return csv.toString();
    }

    /**
     * Activity Diagram: Collect counter definitions (if auto counter enabled)
     */
    private void collectCounterDefinitions() {
        for (Map.Entry<Integer, String> entry : measTypes.entrySet()) {
            String counterName = entry.getValue();
            String definition = "Huawei eNodeB PM counter: " + counterName;
            collectCounterDefinition(counterName, definition);
        }
    }
}
