package com.ttgint.library.nativeQuery;

import com.ttgint.library.record.GenerateColumnRecord;
import com.ttgint.library.record.GenerateIndexRecord;
import com.ttgint.library.record.GeneratePartitionRecord;
import com.ttgint.library.record.GenerateTableRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.List;

@Slf4j
public class MssqlQuery extends NativeQuery {

    public MssqlQuery(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public List<String> getExistsTables(String schemaName) {
        String query
                = "select distinct "
                + "lower(owner + '.' + name) as table_name "
                + "from sys.all_objects "
                + "where type_desc = 'user_table' "
                + (schemaName == null ? "" : "and lower(owner) in ('" + schemaName.toLowerCase() + "') ");
        return super.getResultListForSingleColumn(query, "MssqlQuery getExistsTables");
    }

    @Override
    public Boolean isTableExists(String schemaName, String tableName) {
        String query
                = "select distinct "
                + "lower(owner + '.' + name) as table_name "
                + "from sys.all_objects "
                + "where type_desc = 'user_table' "
                + "and lower(owner) = '" + schemaName.toLowerCase() + "' "
                + "and lower(name) = '" + tableName.toLowerCase() + "' ";
        return !super.getResultListForSingleColumn(query, "MssqlQuery isTableExists").isEmpty();
    }

    @Override
    public List<String> getExistsColumns(String schemaName, String tableName) {
        String query
                = "select distinct "
                + "lower(table_schema + '.' + table_name + '.' + column_name) as column_name "
                + "from information_schema.columns "
                + "where 1=1 "
                + (schemaName == null ? "" : "and lower(table_schema) in ('" + schemaName.toLowerCase() + "') ")
                + (tableName == null ? "" : "and lower(table_name) in ('" + tableName.toLowerCase() + "') ");
        return super.getResultListForSingleColumn(query, "MssqlQuery getExistsColumns");
    }

    @Override
    public Boolean isColumnExists(String schemaName, String tableName, String columnName) {
        String query
                = "select distinct "
                + "lower(table_schema + '.' + table_name + '.' + column_name) as column_name "
                + "from information_schema.columns "
                + "where 1=1 "
                + "and lower(table_schema) = '" + schemaName.toLowerCase() + "' "
                + "and lower(table_name) = '" + tableName.toLowerCase() + "' "
                + "and lower(column_name) = '" + columnName.toLowerCase() + "' ";
        return !super.getResultListForSingleColumn(query, "MssqlQuery isColumnExists").isEmpty();
    }

    @Override
    public Boolean generateTable(GenerateTableRecord record) {
        return false;
    }

    @Override
    public Boolean generatePartition(GeneratePartitionRecord record) {
        return false;
    }

    @Override
    public Boolean generateIndex(GenerateIndexRecord record) {
        return false;
    }

    @Override
    public Boolean generateColumn(GenerateColumnRecord record) {
        return false;
    }

}
