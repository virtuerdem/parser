package com.ttgint.library.model;

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
    private String flowCode;

    private Long allTableId;
    private String schemaName;
    private String tableName;

    private String columnName;
    private Integer columnOrderId;
    private String columnType;
    private String columnFormula;
    private Integer columnLength;
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

}