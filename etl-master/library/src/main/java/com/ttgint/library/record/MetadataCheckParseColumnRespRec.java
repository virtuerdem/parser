package com.ttgint.library.record;

import com.ttgint.library.model.ParseColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class MetadataCheckParseColumnRespRec {

    private Long flowId;
    private Long parseTableId;

    private String schemaName;
    private String tableName;
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
    private Boolean isColumnAgg;
    private String columnAggFormula;

    private Boolean isActive;

    public MetadataCheckParseColumnRespRec(ParseColumn column) {
        this.flowId = column.getFlowId();
        this.parseTableId = column.getParseTableId();
        this.schemaName = column.getSchemaName();
        this.tableName = column.getTableName();
        this.columnName = column.getColumnName();
        this.objectKey = column.getObjectKey();
        this.modelType = column.getModelType();
        this.columnOrderId = column.getColumnOrderId();
        this.columnType = column.getColumnType();
        this.columnLength = column.getColumnLength();
        this.columnFormula = column.getColumnFormula();
        this.isDefaultValue = column.getIsDefaultValue();
        this.columnDefaultValue = column.getColumnDefaultValue();
        this.isColumnGen = column.getIsColumnGen();
        this.columnGenFormula = column.getColumnGenFormula();
        this.isActive = column.getIsActive();
    }

}
