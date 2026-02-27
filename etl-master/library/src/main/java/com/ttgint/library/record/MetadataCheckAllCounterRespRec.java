package com.ttgint.library.record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class MetadataCheckAllCounterRespRec {

    private String counterKey; //objectKey of ParseColumn
    private String counterLookup;
    private String counterDescription;
    private String modelType;

    private Boolean isActive;

}
