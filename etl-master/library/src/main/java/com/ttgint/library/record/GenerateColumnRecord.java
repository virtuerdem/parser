package com.ttgint.library.record;

import com.ttgint.library.model.AllColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Comparator;
import java.util.List;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class GenerateColumnRecord {

    private String schemaName;
    private String tableName;

    private String columnName;
    private Integer columnOrderId;
    private String columnType;
    private Integer columnLength;
    private Boolean isColumnAgg;
    private String columnAggFormula;

    public static GenerateColumnRecord getRecord(AllColumn column) {
        GenerateColumnRecord record = new GenerateColumnRecord();
        record.setSchemaName(column.getSchemaName());
        record.setTableName(column.getTableName());
        record.setColumnName(column.getColumnName());
        record.setColumnOrderId(column.getColumnOrderId());
        record.setColumnType(column.getColumnType());
        record.setColumnLength(column.getColumnLength());
        record.setIsColumnAgg(column.getIsColumnAgg());
        record.setColumnAggFormula(column.getColumnAggFormula());

        return record;
    }

    public static List<GenerateColumnRecord> getRecords(List<AllColumn> columns) {
        return columns.stream()
                .map(GenerateColumnRecord::getRecord)
                .sorted(Comparator.comparingInt(GenerateColumnRecord::getColumnOrderId))
                .toList();
    }
}

