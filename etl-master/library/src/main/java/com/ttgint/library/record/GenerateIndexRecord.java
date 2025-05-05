package com.ttgint.library.record;

import com.ttgint.library.model.AllIndex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class GenerateIndexRecord {

    private String schemaName;
    private String tableName;
    private String indexName;
    private String indexColumnName;
    private String indexTableSpace;

    public static GenerateIndexRecord getRecord(AllIndex index) {
        GenerateIndexRecord record = new GenerateIndexRecord();
        record.setSchemaName(index.getSchemaName());
        record.setTableName(index.getTableName());
        record.setIndexName(index.getIndexName());
        record.setIndexColumnName(index.getIndexColumnName());
        record.setIndexTableSpace(index.getIndexTableSpace());

        return record;
    }

    public static List<GenerateIndexRecord> getRecords(List<AllIndex> indexes) {
        return indexes.stream()
                .map(GenerateIndexRecord::getRecord)
                .toList();
    }
}

