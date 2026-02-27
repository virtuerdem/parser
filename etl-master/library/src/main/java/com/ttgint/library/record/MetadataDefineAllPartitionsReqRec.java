package com.ttgint.library.record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class MetadataDefineAllPartitionsReqRec {

    private Long flowId;
    private Long allTableId;
    private String schemaName;
    private String tableName;
    private List<MetadataDefineAllPartitionReqRec> partitions;

    public MetadataDefineAllPartitionsReqRec(Long flowId) {
        this.flowId = flowId;
    }

}
