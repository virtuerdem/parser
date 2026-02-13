package com.ttgint.library.model;

import com.ttgint.library.record.MetadataDefineAllColumnReqRec;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_all_column")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class AllColumn implements Serializable {

    @Id
    @SequenceGenerator(name = "t_all_column_seq_id", sequenceName = "t_all_column_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_all_column_seq_id")
    private Long id;
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
    private Boolean isColumnGen;
    private String columnGenFormula;
    private Boolean isColumnAgg;
    private String columnAggFormula;

    private Boolean needRefresh;
    private Boolean isGenerated;
    private Boolean isFailed;
    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

    public AllColumn(Long flowId,
                     Long allTableId,
                     String schemaName,
                     String tableName,
                     Integer columnOrderId,
                     MetadataDefineAllColumnReqRec record) {
        this.flowId = flowId;
        this.allTableId = allTableId;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = record.getColumnName() != null ? record.getColumnName() :
                tableName + "c" + String.format("%1$" + 4 + "s", columnOrderId).replace(' ', '0');
        this.objectKey = record.getObjectKey();
        this.objectKey2 = record.getObjectKey2();
        this.columnNameLookup = record.getColumnNameLookup();
        this.columnDescription = record.getColumnDescription();
        this.columnOrderId = columnOrderId;
        this.columnType = record.getColumnType();
        this.columnFormula = record.getColumnFormula();
        this.columnLength = record.getColumnLength();
        this.modelType = record.getModelType();
        this.isColumnGen = record.getIsColumnGen() != null ? record.getIsColumnGen() : false;
        this.columnGenFormula = record.getIsColumnGen() != null && record.getIsColumnGen() ? record.getColumnGenFormula() : null;
        this.isColumnAgg = record.getIsColumnAgg() != null ? record.getIsColumnAgg() : false;
        this.columnAggFormula = record.getIsColumnAgg() != null && record.getIsColumnAgg() ? record.getColumnAggFormula() : null;
        this.needRefresh = true;
        this.isGenerated = false;
        this.isFailed = false;
        this.isActive = true;
        this.createdTime = OffsetDateTime.now();
    }

}