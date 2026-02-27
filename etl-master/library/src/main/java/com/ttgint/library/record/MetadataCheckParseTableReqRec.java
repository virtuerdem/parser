package com.ttgint.library.record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class MetadataCheckParseTableReqRec {

    private Long flowId;
    private Long allTableId;
    private String tableName;
    private String objectKey;
    private String objectType;
    private String elementType;

}
