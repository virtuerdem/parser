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
public class ParseTableRecord {

    private Long allTableId;
    private Long parseTableId;
    private String schemaName;
    private String tableName;

    private String objectType;
    private String objectKey;
    private String objectKeyLookup;

    private Integer dateColumnIndex;
    private String dateColumnName;
    private String resultFileDelimiter;

    private String nodeType;
    private String subNodeType;
    private String elementType;
    private String subElementType;
    private String itemType;
    private String subItemType;

    private String dataSource;
    private String tableGroup;
    private String dataGroup;

    private String groupType;
    private String tableType;
    private String subTableType;
    private String dataType;
    private String networkType;
    private String subNetworkType;

    private String loaderTarget;

    public static ParseTableRecord getRecord(ParseTable table) {
        ParseTableRecord record = new ParseTableRecord();
        record.setAllTableId(table.getAllTableId());
        record.setParseTableId(table.getId());
        record.setSchemaName(table.getSchemaName());
        record.setTableName(table.getTableName());

        record.setObjectType(table.getObjectType());
        record.setObjectKey(table.getObjectKey());
        record.setObjectKeyLookup(table.getObjectKeyLookup());

        record.setDateColumnIndex(table.getDateColumnIndex());
        record.setDateColumnName(table.getDateColumnName());
        record.setResultFileDelimiter(table.getResultFileDelimiter());

        record.setNodeType(table.getNodeType());
        record.setSubNodeType(table.getSubNodeType());
        record.setElementType(table.getElementType());
        record.setSubElementType(table.getSubElementType());
        record.setItemType(table.getItemType());
        record.setSubItemType(table.getSubItemType());

        record.setDataSource(table.getDataSource());
        record.setTableGroup(table.getTableGroup());
        record.setDataGroup(table.getDataGroup());

        record.setGroupType(table.getGroupType());
        record.setTableType(table.getTableType());
        record.setSubTableType(table.getSubTableType());
        record.setDataType(table.getDataType());
        record.setNetworkType(table.getNetworkType());
        record.setSubNetworkType(table.getSubNetworkType());

        record.setLoaderTarget(table.getLoaderTarget());

        return record;
    }

}
