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
public class MetadataCheckAllCountersRespRec {

    private Long flowId;
    private String elementType;
    private String counterGroupType;
    private String counterGroupKey; //objectKey of ParseTable
    private String counterGroupLookup;
    private String counterGroupDescription;

    private List<MetadataCheckAllCounterRespRec> counters;

}
