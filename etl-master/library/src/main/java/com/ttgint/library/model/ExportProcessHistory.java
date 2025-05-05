package com.ttgint.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_export_process_history")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ExportProcessHistory implements Serializable {

    @Id
    @SequenceGenerator(name = "t_export_process_history_seq_id", sequenceName = "t_export_process_history_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_export_process_history_seq_id")
    private Long id;
    private Long flowId;
    private String flowProcessCode;
    private Long exportEngineId;
    private OffsetDateTime flowStartTime;
    private OffsetDateTime flowEndTime;

    private OffsetDateTime createdTime;

}