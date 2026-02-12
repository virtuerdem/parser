package com.telecom.parser.model;

import java.util.List;
import java.util.Map;

/**
 * Table Metadata Model
 * Represents database table structure for CSV mapping
 */
public class TableMetadata {

    private String tableName;
    private String schemaName;
    private List<ColumnMetadata> columns;
    private Map<String, String> counterMapping; // XML counter name -> DB column name

    public TableMetadata() {
    }

    public TableMetadata(String tableName, String schemaName) {
        this.tableName = tableName;
        this.schemaName = schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public void setSchemaName(String schemaName) {
        this.schemaName = schemaName;
    }

    public List<ColumnMetadata> getColumns() {
        return columns;
    }

    public void setColumns(List<ColumnMetadata> columns) {
        this.columns = columns;
    }

    public Map<String, String> getCounterMapping() {
        return counterMapping;
    }

    public void setCounterMapping(Map<String, String> counterMapping) {
        this.counterMapping = counterMapping;
    }

    /**
     * Column Metadata
     */
    public static class ColumnMetadata {
        private String columnName;
        private String dataType;
        private boolean nullable;
        private Integer maxLength;

        public ColumnMetadata() {
        }

        public ColumnMetadata(String columnName, String dataType) {
            this.columnName = columnName;
            this.dataType = dataType;
        }

        public String getColumnName() {
            return columnName;
        }

        public void setColumnName(String columnName) {
            this.columnName = columnName;
        }

        public String getDataType() {
            return dataType;
        }

        public void setDataType(String dataType) {
            this.dataType = dataType;
        }

        public boolean isNullable() {
            return nullable;
        }

        public void setNullable(boolean nullable) {
            this.nullable = nullable;
        }

        public Integer getMaxLength() {
            return maxLength;
        }

        public void setMaxLength(Integer maxLength) {
            this.maxLength = maxLength;
        }
    }
}
