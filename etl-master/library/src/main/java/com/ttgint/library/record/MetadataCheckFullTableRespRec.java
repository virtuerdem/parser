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
public class MetadataCheckFullTableRespRec {

    MetadataCheckAllTableRespRec table;
    List<MetadataCheckAllColumnRespRec> columns;
    List<MetadataCheckPartitionRespRec> partitions;
    List<MetadataCheckAllIndexRespRec> indexes;
    List<MetadataCheckParseMapRespRec> parseMaps;

}
