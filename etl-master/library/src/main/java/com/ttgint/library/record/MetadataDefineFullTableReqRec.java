package com.ttgint.library.record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class MetadataDefineFullTableReqRec {

    private MetadataDefineAllTableReqRec table;
    private MetadataDefineAllColumnsReqRec column;
    private MetadataDefineAllPartitionsReqRec partition;
    private MetadataDefineAllIndexesReqRec index;

}
