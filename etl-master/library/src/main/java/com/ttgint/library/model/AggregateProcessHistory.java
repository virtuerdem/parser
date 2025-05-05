package com.ttgint.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_aggregate_process_history")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class AggregateProcessHistory implements Serializable {

    @Id
    @SequenceGenerator(name = "t_aggregate_process_history_seq_id", sequenceName = "t_aggregate_process_history_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_aggregate_process_history_seq_id")
    private Long id;
    private Long flowId;
    private Long aggregateEngineId;
    private String flowProcessCode;
    private OffsetDateTime flowStartTime;
    private OffsetDateTime flowEndTime;

    private OffsetDateTime createdTime;

}