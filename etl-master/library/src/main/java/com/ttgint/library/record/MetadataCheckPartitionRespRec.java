package com.ttgint.library.record;

import com.ttgint.library.model.AllPartition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class MetadataCheckPartitionRespRec {

    private Long flowId;

    private Long allTableId;
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

    private Boolean isActive;

    public MetadataCheckPartitionRespRec(AllPartition partition) {
        this.flowId = partition.getFlowId();
        this.allTableId = partition.getAllTableId();
        this.schemaName = partition.getSchemaName();
        this.tableName = partition.getTableName();
        this.isRangePartitioned = partition.getIsRangePartitioned();
        this.partitionColumnName = partition.getPartitionColumnName();
        this.partitionStartDate = partition.getPartitionStartDate();
        this.partitionInterval = partition.getPartitionInterval();
        this.partitionPremake = partition.getPartitionPremake();
        this.partitionRetention = partition.getPartitionRetention();
        this.partitionRetentionKeepTable = partition.getPartitionRetentionKeepTable();
        this.partitionDefaultTable = partition.getPartitionDefaultTable();
        this.isCompressed = partition.getIsCompressed();
        this.uncompressedPartition = partition.getUncompressedPartition();
        this.isCompressing = partition.getIsCompressing();
        this.isActive = partition.getIsActive();
    }
}
