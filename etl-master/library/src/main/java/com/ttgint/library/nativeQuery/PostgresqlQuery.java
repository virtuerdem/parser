package com.ttgint.library.nativeQuery;

import com.ttgint.library.record.GenerateColumnRecord;
import com.ttgint.library.record.GenerateIndexRecord;
import com.ttgint.library.record.GeneratePartitionRecord;
import com.ttgint.library.record.GenerateTableRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.List;

@Slf4j
public class PostgresqlQuery extends NativeQuery {

    public PostgresqlQuery(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public List<String> getExistsTables(String schemaName) {
        String query
                = "select distinct "
                + "lower(table_schema || '.' || table_name) as table_name "
                + "from information_schema.tables "
                + "where table_type = 'base table' "
                + (schemaName == null ? "" : "and lower(table_schema) in ('" + schemaName.toLowerCase() + "') ");
        return super.getResultListForSingleColumn(query, "PostgresqlQuery getExistsTables");
    }

    @Override
    public Boolean isTableExists(String schemaName, String tableName) {
        String query
                = "select distinct "
                + "lower(table_schema || '.' || table_name) as table_name "
                + "from information_schema.tables "
                + "where table_type = 'base table' "
                + "and lower(table_schema) = '" + schemaName.toLowerCase() + "' "
                + "and lower(table_name) = '" + tableName.toLowerCase() + "' ";
        return !super.getResultListForSingleColumn(query, "PostgresqlQuery isTableExists").isEmpty();
    }

    @Override
    public List<String> getExistsColumns(String schemaName, String tableName) {
        String query
                = "select distinct "
                + "lower(table_schema || '.' || table_name || '.' || column_name) as column_name "
                + "from information_schema.columns "
                + "where 1=1 "
                + (schemaName == null ? "" : "and lower(table_schema) in ('" + schemaName.toLowerCase() + "') ")
                + (tableName == null ? "" : "and lower(table_name) in ('" + tableName.toLowerCase() + "') ");
        return super.getResultListForSingleColumn(query, "PostgresqlQuery getExistsColumns");
    }

    @Override
    public Boolean isColumnExists(String schemaName, String tableName, String columnName) {
        String query
                = "select distinct "
                + "lower(table_schema || '.' || table_name || '.' || column_name) as column_name "
                + "from information_schema.columns "
                + "where 1=1 "
                + "and lower(table_schema) = '" + schemaName.toLowerCase() + "' "
                + "and lower(table_name) = '" + tableName.toLowerCase() + "' "
                + "and lower(column_name) = '" + columnName.toLowerCase() + "' ";
        return !super.getResultListForSingleColumn(query, "PostgresqlQuery isColumnExists").isEmpty();
    }

    @Override
    public Boolean generateTable(GenerateTableRecord record) {
        StringBuilder tableBuilder = new StringBuilder();
        tableBuilder
                .append("create table ")
                .append(record.getSchemaName())
                .append(".")
                .append(record.getTableName())
                .append(" (")
                .append("\n");

        record.getColumns()
                .forEach(column ->
                        tableBuilder
                                .append(column.getColumnName())
                                .append(" ")
                                .append(column.getColumnType())
                                .append(column.getColumnLength() != null
                                        ? " (" + column.getColumnLength() + ")" : "")
                                .append(column.getIsColumnAgg() != null
                                        && column.getIsColumnAgg()
                                        && column.getColumnAggFormula() != null
                                        ? " generated always as (" + column.getColumnAggFormula() + ") stored" : "")
                                .append(",")
                                .append("\n"));
        tableBuilder.delete(tableBuilder.length() - 2, tableBuilder.length());
        tableBuilder
                .append(")")
                .append("\n");

        if (record.getPartition() != null && record.getPartition().getIsRangePartitioned()) {
            tableBuilder
                    .append("partition by range (")
                    .append(record.getPartition().getPartitionColumnName())
                    .append(")");
        }

        boolean execution = super.executeQuery(tableBuilder.toString().toLowerCase(),
                "generateTable " + record.getSchemaName() + "." + record.getTableName());

        if (execution && record.getPartition() != null && record.getPartition().getIsRangePartitioned()) {
            execution = generatePartition(record.getPartition());
        }

        return execution;
    }

    @Override
    public Boolean generatePartition(GeneratePartitionRecord record) {
        if (record.getIsRangePartitioned()) {
            String partitionBuilder
                    = "select gbd_partman.create_parent( "
                    + "'" + record.getSchemaName() + "." + record.getTableName() + "', "
                    + "'" + record.getPartitionColumnName() + "', "
                    + "'native', "
                    + "'" + record.getPartitionInterval() + "', "
                    + "null, "
                    + record.getPartitionPremake() + ", "
                    + "'on', "
                    + "null, "
                    + "true )";

            String partitionConfig
                    = "update gbd_partman.part_config set "
                    + "retention_keep_table = " + record.getPartitionRetentionKeepTable() + ", "
                    + "retention = '" + record.getPartitionRetention() + "' "
                    + "where parent_table = "
                    + "'" + record.getSchemaName() + "." + record.getTableName() + "'";

            boolean builder = super.executeQuery(partitionBuilder.toLowerCase(),
                    "generatePartition " + record.getSchemaName() + "." + record.getTableName() + "." + record.getPartitionColumnName());

            boolean update = super.executeQuery(partitionConfig.toLowerCase(),
                    "updatePartition " + record.getSchemaName() + "." + record.getTableName() + "." + record.getPartitionColumnName());

            return builder && update;
        }
        return false;
    }

    @Override
    public Boolean generateIndex(GenerateIndexRecord record) {
        String indexBuilder
                = "create index "
                + record.getIndexName() + " "
                + "on "
                + record.getSchemaName() + "." + record.getTableName() + " "
                + "(" + record.getIndexColumnName() + ")";

        return super.executeQuery(indexBuilder.toLowerCase(),
                "generateIndex " + record.getSchemaName() + "." + record.getTableName() + "." + record.getIndexName());
    }

    @Override
    public Boolean generateColumn(GenerateColumnRecord record) {
        String columnBuilder
                = "alter table "
                + record.getSchemaName() + "." + record.getTableName() + " "
                + "add "
                + record.getColumnName() + " "
                + record.getColumnType()
                + (record.getColumnLength() != null
                ? " (" + record.getColumnLength() + ")" : "")
                + (record.getIsColumnAgg() != null
                && record.getIsColumnAgg()
                && record.getColumnAggFormula() != null
                ? " generated always as (" + record.getColumnAggFormula() + ") stored" : "");

        return super.executeQuery(columnBuilder.toLowerCase(),
                "generateColumn " + record.getSchemaName() + "." + record.getTableName() + "." + record.getColumnName());
    }

}
