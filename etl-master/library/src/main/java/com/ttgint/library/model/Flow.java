package com.ttgint.library.model;

import com.ttgint.library.enums.FlowStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_flow",
        uniqueConstraints = {
                @UniqueConstraint(name = "t_flow_ukey_flow_code", columnNames = {"flowCode"})})
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Flow implements Serializable {

    @Id
    @SequenceGenerator(name = "t_flow_seq_id", sequenceName = "t_flow_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_flow_seq_id")
    private Long id;
    private Long branchId;
    private String flowCode;
    @Enumerated(EnumType.STRING)
    private FlowStatus flowStatus;
    private Long rootManagerId;
    private Long activeManagerId;
    private Boolean isMovable;
    private String flowCron;
    private String flowInfo;
    private Long dependFlowId;

    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

}
