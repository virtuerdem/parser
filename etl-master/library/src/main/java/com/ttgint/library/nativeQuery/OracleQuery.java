package com.ttgint.library.nativeQuery;

import com.ttgint.library.record.GenerateColumnRecord;
import com.ttgint.library.record.GenerateIndexRecord;
import com.ttgint.library.record.GeneratePartitionRecord;
import com.ttgint.library.record.GenerateTableRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class OracleQuery extends NativeQuery {

    public OracleQuery(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public List<String> getExistsTables(String schemaName) {
        String query
                = "select distinct "
                + "lower(owner || '.' || table_name) as table_name "
                + "from all_tables "
                + "where 1=1 "
                + (schemaName == null ? "" : "and lower(owner) in (" + schemaName.toLowerCase() + ") ");
        return super.getResultListForSingleColumn(query, "OracleQuery getExistsTables");
    }

    @Override
    public Boolean isTableExists(String schemaName, String tableName) {
        String query
                = "select distinct "
                + "lower(owner || '.' || table_name) as table_name "
                + "from all_tables "
                + "where 1=1 "
                + "and lower(owner) = '" + schemaName.toLowerCase() + "' "
                + "and lower(table_name) = '" + tableName.toLowerCase() + "' ";
        return !super.getResultListForSingleColumn(query, "OracleQuery isTableExists").isEmpty();
    }

    @Override
    public List<String> getExistsColumns(String schemaName, String tableName) {
        String query
                = "select distinct "
                + "lower(owner || '.' || table_name || '.' || column_name) as column_name "
                + "from all_tab_cols "
                + "where 1=1 "
                + (schemaName == null ? "" : "and lower(owner) in ('" + schemaName.toLowerCase() + "') ")
                + (tableName == null ? "" : "and lower(table_name) in ('" + tableName.toLowerCase() + "') ");
        return super.getResultListForSingleColumn(query, "OracleQuery getExistsColumns");
    }

    @Override
    public Boolean isColumnExists(String schemaName, String tableName, String columnName) {
        String query
                = "select distinct "
                + "lower(owner || '.' || table_name || '.' || column_name) as column_name "
                + "from all_tab_cols "
                + "where 1=1 "
                + "and lower(owner) = '" + schemaName.toLowerCase() + "' "
                + "and lower(table_name) = '" + tableName.toLowerCase() + "' "
                + "and lower(column_name) = '" + columnName.toLowerCase() + "' ";
        return !super.getResultListForSingleColumn(query, "OracleQuery isColumnExists").isEmpty();
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
                                        ? " (" + column.getColumnLength() + " byte)" : "")
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
                    .append(")")
                    .append("\n")
                    .append("interval(numtodsinterval(1, '")
                    .append(record.getPartition().getPartitionInterval().toUpperCase())
                    .append("')) ")
                    .append("\n")
                    .append("(partition tp_")
                    .append(record.getPartition().getPartitionStartDate()
                            .format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH")))
                    .append(" values less than (to_date('")
                    .append(record.getPartition().getPartitionStartDate()
                            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH")))
                    .append("','yyyy-mm-dd-hh24')) ")
                    .append("logging compress for oltp) ")
                    .append("\n")
                    .append("monitoring ")
                    .append("\n")
                    .append("enable row movement ")
                    .append("\n")
                    .append("compress for oltp ")
                    .append("\n")
                    .append("noparallel ")
                    .append("\n")
                    .append("storage (initial 64k next 64k)");
        }
        return super.executeQuery(tableBuilder.toString().toLowerCase(),
                "generateTable " + record.getSchemaName() + "." + record.getTableName());
    }

    @Override
    public Boolean generatePartition(GeneratePartitionRecord record) {
        if (record.getIsRangePartitioned()) {
            String partitionBuilder
                    = "alter table "
                    + record.getSchemaName() + "." + record.getTableName() + " "
                    + "modify "
                    + "\n"
                    + "partition by range ("
                    + record.getPartitionColumnName()
                    + ")"
                    + "\n"
                    + "interval(numtodsinterval(1, '"
                    + record.getPartitionInterval().toUpperCase()
                    + "')) "
                    + "\n"
                    + "(partition tp_"
                    + record.getPartitionStartDate().format(DateTimeFormatter.ofPattern("yyyy_MM_dd_HH"))
                    + " "
                    + "values less than (to_date('"
                    + record.getPartitionStartDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"))
                    + "','yyyy-mm-dd-hh24')) "
                    + "logging compress for oltp)";

            return super.executeQuery(partitionBuilder.toLowerCase(),
                    "generatePartition " + record.getSchemaName() + "." + record.getTableName() + "." + record.getPartitionColumnName());
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
                + "(" + record.getIndexColumnName() + ") "
                + "logging local compress 1 compute statistics noparallel "
                + (record.getIndexTableSpace() != null
                ? "tablespace " + record.getIndexTableSpace() : "");

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
                ? " (" + record.getColumnLength() + " byte)" : "");

        return super.executeQuery(columnBuilder.toLowerCase(),
                "generateColumn " + record.getSchemaName() + "." + record.getTableName() + "." + record.getColumnName());
    }

}
