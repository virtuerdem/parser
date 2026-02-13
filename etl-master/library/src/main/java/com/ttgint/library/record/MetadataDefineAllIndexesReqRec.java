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
public class MetadataDefineAllIndexesReqRec {

    private Long flowId;
    private Long allTableId;
    private String schemaName;
    private String tableName;
    private List<MetadataDefineAllIndexReqRec> indexes;

    public MetadataDefineAllIndexesReqRec(Long flowId) {
        this.flowId = flowId;
    }

}
