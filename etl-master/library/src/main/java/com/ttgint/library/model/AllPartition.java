package com.ttgint.library.model;

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
    private String flowCode;

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

}