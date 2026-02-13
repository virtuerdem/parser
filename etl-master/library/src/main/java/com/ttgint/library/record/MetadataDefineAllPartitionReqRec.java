package com.ttgint.library.record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class MetadataDefineAllPartitionReqRec {

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

    public MetadataDefineAllPartitionReqRec(Boolean isRangePartitioned) {
        this.isRangePartitioned = isRangePartitioned;
        this.partitionColumnName = "fragment_date";
        this.partitionStartDate = OffsetDateTime.now().minusDays(7);
        this.partitionInterval = "1 day";
        this.partitionPremake = 30L;
        this.partitionRetention = "365 days";
        this.partitionRetentionKeepTable = "false";
        this.partitionDefaultTable = null;
        this.isCompressed = false;
        this.uncompressedPartition = null;
        this.isCompressing = false;
    }

}
