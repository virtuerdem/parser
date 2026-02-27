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
public class MetadataCheckAllColumnReqRec {

    private Long flowId;
    private Long allTableId;
    private String tableName;
    private List<String> columnNames;
    private List<String> objectKeys;

}
