package com.ttgint.library.model;

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
    private String objectKeyLookup;
    private String objectKeyDescription;

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
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

}