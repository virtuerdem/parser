package com.ttgint.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_transfer_connection_history")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class TransferConnectionHistory implements Serializable {

    @Id
    @SequenceGenerator(name = "t_transfer_connection_history_seq_id", sequenceName = "t_transfer_connection_history_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_transfer_connection_history_seq_id")
    private Long id;
    private Long flowId;
    private String flowProcessCode;
    private Long connectionId;
    private String connectionHistoryCode;
    private Boolean isConnected;
    private OffsetDateTime connectionStartTime;
    private OffsetDateTime readStartTime;
    private Long readFileCount;
    private Long readFileSize;
    private OffsetDateTime readEndTime;
    private OffsetDateTime transferStartTime;
    private Long transferFileCount;
    private Long transferFileSize;
    private OffsetDateTime transferEndTime;
    private OffsetDateTime connectionEndTime;

}