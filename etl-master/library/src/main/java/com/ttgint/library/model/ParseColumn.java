package com.ttgint.library.model;

import com.ttgint.library.record.MetadataDefineParseColumnReqRec;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_parse_column")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ParseColumn implements Serializable {

    @Id
    @SequenceGenerator(name = "t_parse_column_seq_id", sequenceName = "t_parse_column_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_parse_column_seq_id")
    private Long id;
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

    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

    public ParseColumn(Long flowId,
                       Long parseTableId,
                       String schemaName,
                       String tableName,
                       MetadataDefineParseColumnReqRec record) {
        this.flowId = flowId;
        this.parseTableId = parseTableId;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = record.getColumnName();
        this.objectKey = record.getObjectKey();
        this.objectKey2 = record.getObjectKey2();
        this.modelType = record.getModelType();
        this.columnOrderId = record.getColumnOrderId();
        this.columnType = record.getColumnType();
        this.columnLength = record.getColumnLength();
        this.columnFormula = record.getColumnFormula();
        this.isDefaultValue = record.getIsDefaultValue() != null ? record.getIsDefaultValue() : false;
        this.columnDefaultValue = record.getIsDefaultValue() != null && record.getIsDefaultValue() ? record.getColumnDefaultValue() : null;
        this.isColumnGen = record.getIsColumnGen() != null ? record.getIsColumnGen() : false;
        this.columnGenFormula = record.getIsColumnGen() != null && record.getIsColumnGen() ? record.getColumnGenFormula() : null;
        this.isActive = true;
        this.createdTime = OffsetDateTime.now();
    }
}