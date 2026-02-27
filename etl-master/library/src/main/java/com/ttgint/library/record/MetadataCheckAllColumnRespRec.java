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
public class MetadataCheckAllColumnRespRec {

    private Long flowId;

    private Long allTableId;
    private String schemaName;
    private String tableName;

    private String columnName;
    private String objectKey;
    private String objectKey2;
    private String columnNameLookup;
    private String columnDescription;

    private Integer columnOrderId;
    private String columnType;
    private String columnFormula;
    private Integer columnLength;
    private String modelType;
    private Boolean isColumnAgg;
    private String columnAggFormula;

    private Boolean isActive;

    public MetadataCheckAllColumnRespRec(AllColumn column) {
        this.flowId = column.getFlowId();
        this.allTableId = column.getAllTableId();
        this.schemaName = column.getSchemaName();
        this.tableName = column.getTableName();
        this.columnName = column.getColumnName();
        this.objectKey = column.getObjectKey();
        this.objectKey2 = column.getObjectKey2();
        this.columnNameLookup = column.getColumnNameLookup();
        this.columnDescription = column.getColumnDescription();
        this.columnOrderId = column.getColumnOrderId();
        this.columnType = column.getColumnType();
        this.columnFormula = column.getColumnFormula();
        this.columnLength = column.getColumnLength();
        this.modelType = column.getModelType();
        this.isColumnAgg = column.getIsColumnAgg();
        this.columnAggFormula = column.getColumnAggFormula();
        this.isActive = column.getIsActive();
    }

}
