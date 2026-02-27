package com.ttgint.library.model;

import com.ttgint.library.record.MetadataDefineAllPartitionReqRec;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_all_partition")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class AllPartition implements Serializable {

    @Id
    @SequenceGenerator(name = "t_all_partition_seq_id", sequenceName = "t_all_partition_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_all_partition_seq_id")
    private Long id;
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

    private Boolean needRefresh;
    private Boolean isGenerated;
    private Boolean isFailed;
    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

    public AllPartition(Long flowId,
                        Long allTableId,
                        String schemaName,
                        String tableName,
                        MetadataDefineAllPartitionReqRec record) {
        this.flowId = flowId;
        this.allTableId = allTableId;
        this.schemaName = schemaName;
        this.tableName = tableName;

        this.isRangePartitioned = record.getIsRangePartitioned();
        this.partitionColumnName = record.getPartitionColumnName();
        this.partitionStartDate = record.getPartitionStartDate();
        this.partitionInterval = record.getPartitionInterval();
        this.partitionPremake = record.getPartitionPremake();
        this.partitionRetention = record.getPartitionRetention();
        this.partitionRetentionKeepTable = record.getPartitionRetentionKeepTable();
        this.partitionDefaultTable = record.getPartitionDefaultTable();

        this.isCompressed = record.getIsCompressed();
        this.uncompressedPartition = record.getUncompressedPartition();
        this.isCompressing = record.getIsCompressing();

        this.needRefresh = true;
        this.isGenerated = false;
        this.isFailed = false;
        this.isActive = true;
        this.createdTime = OffsetDateTime.now();
    }

}