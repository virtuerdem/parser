package com.ttgint.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_aggregate_engine")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class AggregateEngine implements Serializable {

    @Id
    @SequenceGenerator(name = "t_aggregate_engine_seq_id", sequenceName = "t_aggregate_engine_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_aggregate_engine_seq_id")
    private Long id;
    private Long flowId;
    private Long aggregateComponentId;

    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

}