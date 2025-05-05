package com.ttgint.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_all_table")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class AllTable implements Serializable {

    @Id
    @SequenceGenerator(name = "t_all_table_seq_id", sequenceName = "t_all_table_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_all_table_seq_id")
    private Long id;
    private Long flowId;
    private String flowCode;

    private String schemaName;
    private String tableName;

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