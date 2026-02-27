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
public class MetadataDefineParseTableReqRec {

    private Long flowId;
    private Long allTableId;
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

    public MetadataDefineParseTableReqRec(AllTable table) {
        this.flowId = table.getFlowId();
        this.allTableId = table.getId();
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

        this.dateColumnIndex = 5;
        this.dateColumnName = "fragment_date";
        this.resultFileDelimiter = "|";
    }

}
