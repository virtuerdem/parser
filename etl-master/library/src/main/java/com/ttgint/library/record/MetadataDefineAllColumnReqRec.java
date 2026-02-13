package com.ttgint.library.record;

import com.ttgint.library.model.AllCounter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class MetadataDefineAllColumnReqRec {

    private String columnName;
    private String objectKey;
    private String objectKey2;
    private String columnNameLookup;
    private String columnDescription;

    private String columnType;
    private String columnFormula;
    private Integer columnLength;
    private String modelType;
    private Boolean isColumnGen;
    private String columnGenFormula;
    private Boolean isColumnAgg;
    private String columnAggFormula;

    public MetadataDefineAllColumnReqRec(AllCounter counter) {
        this.objectKey = counter.getCounterKey();
        this.columnNameLookup = counter.getCounterLookup();
        this.columnDescription = counter.getCounterDescription();
        this.modelType = counter.getModelType();
    }

}
