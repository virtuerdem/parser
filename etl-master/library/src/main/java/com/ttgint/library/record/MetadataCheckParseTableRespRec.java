package com.ttgint.library.record;

import com.ttgint.library.model.ParseTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class MetadataCheckParseTableRespRec {

    private Long flowId;
    private Long allTableId;
    private Long parseTableId;

    private String schemaName;
    private String tableName;

    private String objectKey;
    private String objectType;
    private String elementType;
    private String nodeType;
    private String itemType;
    private String tableType;
    private String subTableType;
    private String networkType;
    private String subNetworkType;

    private String groupType;
    private String dataType;
    private String dataSource;
    private String tableGroup;
    private String dataGroup;

    private Integer dateColumnIndex;
    private String dateColumnName;
    private String resultFileDelimiter;

    private String loaderTarget;

    private Boolean isActive;

    public MetadataCheckParseTableRespRec(ParseTable table) {
        this.flowId = table.getFlowId();
        this.allTableId = table.getAllTableId();
        this.parseTableId = table.getId();

        this.schemaName = table.getSchemaName();
        this.tableName = table.getTableName();

        this.objectKey = table.getObjectKey();
        this.objectType = table.getObjectType();
        this.elementType = table.getElementType();
        this.nodeType = table.getNodeType();
        this.itemType = table.getItemType();
        this.tableType = table.getTableType();
        this.subTableType = table.getSubTableType();
        this.networkType = table.getNetworkType();
        this.subNetworkType = table.getSubNetworkType();

        this.groupType = table.getGroupType();
        this.dataType = table.getDataType();
        this.dataSource = table.getDataSource();
        this.tableGroup = table.getTableGroup();
        this.dataGroup = table.getDataGroup();

        this.dateColumnIndex = table.getDateColumnIndex();
        this.dateColumnName = table.getDateColumnName();
        this.resultFileDelimiter = table.getResultFileDelimiter();
        this.loaderTarget = table.getLoaderTarget();
        this.isActive = table.getIsActive();
    }

}
