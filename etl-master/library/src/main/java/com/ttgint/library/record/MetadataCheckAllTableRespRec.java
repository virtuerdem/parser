package com.ttgint.library.record;

import com.ttgint.library.model.AllTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class MetadataCheckAllTableRespRec {

    private Long flowId;
    private Long allTableId;

    private String dbCatalog;
    private String schemaName;
    private String tableName;
    private String tablespaceName;
    private String tableNameAlias;
    private String tableNameLookup;
    private String tableDescription;

    private String objectKey;
    private String objectKey2;
    private String objectType;
    private String elementType;
    private String nodeType;
    private String itemType;
    private String tableType;
    private String subTableType;
    private String networkType;
    private String subNetworkType;

    private String dataType;
    private String dataInterval;
    private Long timePeriod;
    private Long timeDelay;

    private Boolean isActive;

    public MetadataCheckAllTableRespRec(AllTable table) {
        this.flowId = table.getFlowId();
        this.allTableId = table.getId();

        this.dbCatalog = table.getDbCatalog();
        this.schemaName = table.getSchemaName();
        this.tableName = table.getTableName();
        this.tablespaceName = table.getTablespaceName();
        this.tableNameAlias = table.getTableNameAlias();
        this.tableNameLookup = table.getTableNameLookup();
        this.tableDescription = table.getTableDescription();

        this.objectKey = table.getObjectKey();
        this.objectKey2 = table.getObjectKey2();
        this.objectType = table.getObjectType();
        this.elementType = table.getElementType();
        this.nodeType = table.getNodeType();
        this.itemType = table.getItemType();
        this.tableType = table.getTableType();
        this.subTableType = table.getSubTableType();
        this.networkType = table.getNetworkType();
        this.subNetworkType = table.getSubNetworkType();

        this.dataType = table.getDataType();
        this.dataInterval = table.getDataInterval();
        this.timePeriod = table.getTimePeriod();
        this.timeDelay = table.getTimeDelay();
        this.isActive = table.getIsActive();
    }

}
