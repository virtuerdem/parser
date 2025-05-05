package com.ttgint.library.record;

import com.ttgint.library.model.ParseColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ParseColumnRecord {

    private Long parseTableId;

    private String schemaName;
    private String tableName;
    private String columnName;

    private String objectKey;
    private String objectKeyLookup;
    private String objectKeyDescription;

    private String modelType;
    private Integer columnOrderId;
    private String columnType;
    private Integer columnLength;
    private String columnFormula;

    private Boolean isDefaultValue;
    private String columnDefaultValue;
    private Boolean isColumnGen;
    private String columnGenFormula;
    private Boolean isColumnAgg;
    private String columnAggFormula;

    public static ParseColumnRecord getRecord(ParseColumn column) {
        ParseColumnRecord record = new ParseColumnRecord();
        record.setParseTableId(column.getParseTableId());

        record.setSchemaName(column.getSchemaName());
        record.setTableName(column.getTableName());
        record.setColumnName(column.getColumnName());

        record.setObjectKey(column.getObjectKey());
        record.setObjectKeyLookup(column.getObjectKeyLookup());
        record.setObjectKeyDescription(column.getObjectKeyDescription());

        record.setModelType(column.getModelType());
        record.setColumnOrderId(column.getColumnOrderId());
        record.setColumnType(column.getColumnType());
        record.setColumnLength(column.getColumnLength());
        record.setColumnFormula(column.getColumnFormula());

        record.setIsDefaultValue(column.getIsDefaultValue());
        record.setColumnDefaultValue(column.getColumnDefaultValue());
        record.setIsColumnGen(column.getIsColumnGen());
        record.setColumnGenFormula(column.getColumnGenFormula());
        record.setIsColumnAgg(column.getIsColumnAgg());
        record.setColumnAggFormula(column.getColumnAggFormula());

        return record;
    }

}
