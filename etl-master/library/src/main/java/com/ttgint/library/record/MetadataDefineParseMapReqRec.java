package com.ttgint.library.record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class MetadataDefineParseMapReqRec {

    private MetadataDefineParseTableReqRec table;
    private MetadataDefineParseColumnsReqRec columns;

}
