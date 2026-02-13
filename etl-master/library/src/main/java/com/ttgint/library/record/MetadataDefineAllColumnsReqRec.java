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
public class MetadataDefineAllColumnsReqRec {

    private Long flowId;
    private Long allTableId;
    private String schemaName;
    private String tableName;
    private List<MetadataDefineAllColumnReqRec> columns;

    public MetadataDefineAllColumnsReqRec(Long flowId) {
        this.flowId = flowId;
    }

}
