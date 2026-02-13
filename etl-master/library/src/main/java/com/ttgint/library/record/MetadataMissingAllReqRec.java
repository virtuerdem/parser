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
public class MetadataMissingAllReqRec {

    private Long flowId;
    private String elementType;
    private String counterGroupType;
    private String counterGroupKey;
    private Long timePeriod;
    private List<String> counterKeys;

}
