package com.ttgint.library.record;

import com.ttgint.library.model.AllColumn;
import com.ttgint.library.model.AllIndex;
import com.ttgint.library.model.AllPartition;
import com.ttgint.library.model.AllTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class GenerateTableRecord {

    private String schemaName;
    private String tableName;

    private List<GenerateColumnRecord> columns;

    private GeneratePartitionRecord partition;

    private List<GenerateIndexRecord> indexes;

    public static GenerateTableRecord getRecord(AllTable table, List<AllColumn> columns) {
        GenerateTableRecord record = new GenerateTableRecord();
        record.setSchemaName(table.getSchemaName());
        record.setTableName(table.getTableName());
        record.setColumns(GenerateColumnRecord.getRecords(columns));

        return record;
    }

    public static GenerateTableRecord getRecord(AllTable table,
                                                List<AllColumn> columns,
                                                List<AllIndex> indexes,
                                                AllPartition partition) {
        GenerateTableRecord record = getRecord(table, columns);
        if (partition != null) {
            record.setPartition(GeneratePartitionRecord.getRecord(partition));
        }
        record.setIndexes(GenerateIndexRecord.getRecords(indexes));

        return record;
    }
}
