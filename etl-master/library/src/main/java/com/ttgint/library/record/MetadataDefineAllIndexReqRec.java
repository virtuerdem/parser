package com.ttgint.library.record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class MetadataDefineAllIndexReqRec {

    private String indexName;
    private String indexColumnName;
    private String indexTableSpace;

    public MetadataDefineAllIndexReqRec(String indexColumnName) {
        this.indexColumnName = indexColumnName;
    }

}
