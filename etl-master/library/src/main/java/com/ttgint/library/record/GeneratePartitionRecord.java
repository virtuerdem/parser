package com.ttgint.library.record;

import com.ttgint.library.model.AllPartition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class GeneratePartitionRecord {

    private String schemaName;
    private String tableName;

    private Boolean isRangePartitioned;
    private String partitionColumnName;
    private OffsetDateTime partitionStartDate;
    private String partitionInterval;
    private Long partitionPremake;
    private String partitionRetention;
    private String partitionRetentionKeepTable;
    private String partitionDefaultTable;

    private Boolean isCompressed;
    private String uncompressedPartition;
    private Boolean isCompressing;

    public static GeneratePartitionRecord getRecord(AllPartition partition) {
        GeneratePartitionRecord record = new GeneratePartitionRecord();
        record.setSchemaName(partition.getSchemaName());
        record.setTableName(partition.getTableName());
        record.setIsRangePartitioned(partition.getIsRangePartitioned());
        record.setPartitionColumnName(partition.getPartitionColumnName());
        record.setPartitionStartDate(partition.getPartitionStartDate());
        record.setPartitionInterval(partition.getPartitionInterval());
        record.setPartitionPremake(partition.getPartitionPremake());
        record.setPartitionRetention(partition.getPartitionRetention());
        record.setPartitionRetentionKeepTable(partition.getPartitionRetentionKeepTable());
        record.setPartitionDefaultTable(partition.getPartitionDefaultTable());
        record.setIsCompressed(partition.getIsCompressed());
        record.setUncompressedPartition(partition.getUncompressedPartition());
        record.setIsCompressing(partition.getIsCompressing());

        return record;
    }

    public static List<GeneratePartitionRecord> getRecords(List<AllPartition> partitions) {
        return partitions.stream()
                .map(GeneratePartitionRecord::getRecord)
                .toList();
    }
}

