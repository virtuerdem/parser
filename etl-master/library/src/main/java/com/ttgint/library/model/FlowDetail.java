package com.ttgint.library.model;

import com.ttgint.library.enums.EngineType;
import com.ttgint.library.enums.FlowStatus;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_flow_detail")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class FlowDetail implements Serializable {

    @Id
    @SequenceGenerator(name = "t_flow_detail_seq_id", sequenceName = "t_flow_detail_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_flow_detail_seq_id")
    private Long id;
    private Long flowId;
    @Enumerated(EnumType.STRING)
    private EngineType engineType;
    private Long engineId;
    @Enumerated(EnumType.STRING)
    private FlowStatus engineStatus;
    private Integer executeOrder;

    private Boolean isJvmParam;
    private String jvmParam;

    private String lastExecutePid;
    private OffsetDateTime lastExecuteTime;
    private Boolean isContinueAfterTimout;
    private Integer timeoutThresholdMin;

    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

}
