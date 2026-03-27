package com.telecom.parser.model;

import java.util.Map;

/**
 * Parse Engine Configuration Record
 * Contains all configuration needed to start a parse flow
 */
public class ParseEngineRecord {

    // Flow identification
    private Long flowId;
    private String flowName;
    private String vendor; // HUAWEI, ERICSSON, NOKIA, ZTE
    private String technology; // 4G, 5G, 3G
    private String dataType; // PM, CM, CONF

    // Paths
    private String rawPath;      // Input XML files path
    private String resultPath;   // Output CSV files path
    private String errorPath;    // Failed parse files path

    // Database configuration
    private Long branchId;       // Network branch ID
    private String schemaName;   // Database schema

    // Thread pool configuration
    private Integer threadPoolSize; // Default: 8
    private Integer maxMemoryMB;    // Default: 4096

    // Feature flags (from activity diagram decision nodes)
    private Boolean isActiveFetchTables;
    private Boolean isActivePreParse;
    private Boolean isActiveOnParse;
    private Boolean isActivePostParse;
    private Boolean isActiveAutoCounter;
    private Boolean isActiveDiscoverContentDate;
    private Boolean isActiveCleanDuplicateBefore;
    private Boolean isActiveCleanDuplicateAfter;
    private Boolean isActiveCallProcedure;
    private Boolean isActiveCallAggregate;
    private Boolean isActiveCallExport;

    // Additional configuration
    private Map<String, Object> customConfig;

    // Constructors
    public ParseEngineRecord() {
        // Set defaults
        this.threadPoolSize = 8;
        this.maxMemoryMB = 4096;
        this.isActiveOnParse = true;
    }

    public ParseEngineRecord(Long flowId, String rawPath, String resultPath, String errorPath) {
        this();
        this.flowId = flowId;
        this.rawPath = rawPath;
        this.resultPath = resultPath;
        this.errorPath = errorPath;
    }

    // Getters and Setters
    public Long getFlowId() {
        return flowId;
    }

    public void setFlowId(Long flowId) {
        this.flowId = flowId;
    }

    public String getFlowName() {
        return flowName;
    }

    public void setFlowName(String flowName) {
        this.flowName = flowName;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getTechnology() {
        return technology;
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getRawPath() {
        return rawPath;
    }

    public void setRawPath(String rawPath) {
        this.rawPath = rawPath;
    }

    public String getResultPath() {
        return resultPath;
    }

    public void setResultPath(String resultPath) {
        this.resultPath = resultPath;
    }

    public String getErrorPath() {
        return errorPath;
    }

    public void setErrorPath(String errorPath) {
        this.errorPath = errorPath;
    }

    public Long getBranchId() {
        return branchId;
    }

    public void setBranchId(Long branchId) {
        this.branchId = branchId;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public Integer getThreadPoolSize() {
        return threadPoolSize;
    }

    public void setThreadPoolSize(Integer threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    public Integer getMaxMemoryMB() {
        return maxMemoryMB;
    }

    public void setMaxMemoryMB(Integer maxMemoryMB) {
        this.maxMemoryMB = maxMemoryMB;
    }

    public Boolean getIsActiveFetchTables() {
        return isActiveFetchTables;
    }

    public void setIsActiveFetchTables(Boolean isActiveFetchTables) {
        this.isActiveFetchTables = isActiveFetchTables;
    }

    public Boolean getIsActivePreParse() {
        return isActivePreParse;
    }

    public void setIsActivePreParse(Boolean isActivePreParse) {
        this.isActivePreParse = isActivePreParse;
    }

    public Boolean getIsActiveOnParse() {
        return isActiveOnParse;
    }

    public void setIsActiveOnParse(Boolean isActiveOnParse) {
        this.isActiveOnParse = isActiveOnParse;
    }

    public Boolean getIsActivePostParse() {
        return isActivePostParse;
    }

    public void setIsActivePostParse(Boolean isActivePostParse) {
        this.isActivePostParse = isActivePostParse;
    }

    public Boolean getIsActiveAutoCounter() {
        return isActiveAutoCounter;
    }

    public void setIsActiveAutoCounter(Boolean isActiveAutoCounter) {
        this.isActiveAutoCounter = isActiveAutoCounter;
    }

    public Boolean getIsActiveDiscoverContentDate() {
        return isActiveDiscoverContentDate;
    }

    public void setIsActiveDiscoverContentDate(Boolean isActiveDiscoverContentDate) {
        this.isActiveDiscoverContentDate = isActiveDiscoverContentDate;
    }

    public Boolean getIsActiveCleanDuplicateBefore() {
        return isActiveCleanDuplicateBefore;
    }

    public void setIsActiveCleanDuplicateBefore(Boolean isActiveCleanDuplicateBefore) {
        this.isActiveCleanDuplicateBefore = isActiveCleanDuplicateBefore;
    }

    public Boolean getIsActiveCleanDuplicateAfter() {
        return isActiveCleanDuplicateAfter;
    }

    public void setIsActiveCleanDuplicateAfter(Boolean isActiveCleanDuplicateAfter) {
        this.isActiveCleanDuplicateAfter = isActiveCleanDuplicateAfter;
    }

    public Boolean getIsActiveCallProcedure() {
        return isActiveCallProcedure;
    }

    public void setIsActiveCallProcedure(Boolean isActiveCallProcedure) {
        this.isActiveCallProcedure = isActiveCallProcedure;
    }

    public Boolean getIsActiveCallAggregate() {
        return isActiveCallAggregate;
    }

    public void setIsActiveCallAggregate(Boolean isActiveCallAggregate) {
        this.isActiveCallAggregate = isActiveCallAggregate;
    }

    public Boolean getIsActiveCallExport() {
        return isActiveCallExport;
    }

    public void setIsActiveCallExport(Boolean isActiveCallExport) {
        this.isActiveCallExport = isActiveCallExport;
    }

    public Map<String, Object> getCustomConfig() {
        return customConfig;
    }

    public void setCustomConfig(Map<String, Object> customConfig) {
        this.customConfig = customConfig;
    }

    @Override
    public String toString() {
        return "ParseEngineRecord{" +
                "flowId=" + flowId +
                ", flowName='" + flowName + '\'' +
                ", vendor='" + vendor + '\'' +
                ", technology='" + technology + '\'' +
                ", dataType='" + dataType + '\'' +
                ", threadPoolSize=" + threadPoolSize +
                '}';
    }
}
