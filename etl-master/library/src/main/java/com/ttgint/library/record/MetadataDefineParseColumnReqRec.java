package com.ttgint.library.record;

import com.ttgint.library.model.AllColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class MetadataDefineParseColumnReqRec {

    private String columnName;
    private String objectKey;
    private String objectKey2;

    private String modelType;
    private Integer columnOrderId;
    private String columnType;
    private Integer columnLength;
    private String columnFormula;

    private Boolean isDefaultValue;
    private String columnDefaultValue;
    private Boolean isColumnGen;
    private String columnGenFormula;

    public MetadataDefineParseColumnReqRec(AllColumn column) {
        this.columnName = column.getColumnName();
        this.objectKey = column.getObjectKey();
        this.objectKey2 = column.getObjectKey2();
        this.modelType = column.getModelType();
        this.columnOrderId = column.getColumnOrderId();
        this.columnType = column.getColumnType();
        this.columnLength = column.getColumnLength();
        this.columnFormula = column.getColumnFormula();
        this.isColumnGen = column.getIsColumnGen();
        this.columnGenFormula = column.getColumnGenFormula();
    }

}
