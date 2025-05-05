package com.ttgint.library.record;

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
public class SqlLdrColumnPatternRecord {

    private String columnName;

    private String modelType;
    private Integer columnOrderId;
    private String columnType;
    private Integer columnLength;
    private String columnFormula;

    private Boolean isColumnAgg;
    private String columnAggFormula;

    public static SqlLdrColumnPatternRecord getRecord(ParseColumnRecord column) {
        SqlLdrColumnPatternRecord record = new SqlLdrColumnPatternRecord();
        record.setColumnName(column.getColumnName());

        record.setModelType(column.getModelType());
        record.setColumnOrderId(column.getColumnOrderId());
        record.setColumnType(column.getColumnType());
        record.setColumnLength(column.getColumnLength());
        record.setColumnFormula(column.getColumnFormula());

        record.setIsColumnAgg(column.getIsColumnAgg());
        record.setColumnAggFormula(column.getColumnAggFormula());
        return record;
    }

    public static List<SqlLdrColumnPatternRecord> getRecords(List<ParseColumnRecord> columns) {
        return columns.stream()
                .map(SqlLdrColumnPatternRecord::getRecord)
                .sorted(Comparator.comparingInt(SqlLdrColumnPatternRecord::getColumnOrderId))
                .toList();
    }
}
