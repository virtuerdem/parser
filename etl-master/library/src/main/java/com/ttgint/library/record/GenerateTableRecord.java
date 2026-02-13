package com.ttgint.library.record;

import com.ttgint.library.model.AllColumn;
import com.ttgint.library.model.AllPartition;
import com.ttgint.library.model.AllTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Optional;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class GenerateTableRecord {

    private String schemaName;
    private String tableName;

    private List<GenerateColumnRecord> columns;

    private GeneratePartitionRecord partition;
    //private List<GenerateIndexRecord> indexes;

    public static GenerateTableRecord getRecord(AllTable table, List<AllColumn> columns) {
        GenerateTableRecord record = new GenerateTableRecord();
        record.setSchemaName(table.getSchemaName());
        record.setTableName(table.getTableName());
        record.setColumns(GenerateColumnRecord.getRecords(columns));

        return record;
    }

    public static GenerateTableRecord getRecord(AllTable table,
                                                List<AllColumn> columns,
                                                Optional<AllPartition> partition) {
        GenerateTableRecord record = getRecord(table, columns);
        partition.ifPresent(e -> record.setPartition(GeneratePartitionRecord.getRecord(e)));
        return record;
    }
}
