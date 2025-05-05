package com.ttgint.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_manager")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Manager implements Serializable {

    @Id
    @SequenceGenerator(name = "t_manager_seq_id", sequenceName = "t_manager_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_manager_seq_id")
    private Long id;
    private String managerCode;
    private String managerName;
    private Long machineId;
    private Integer port;

    private String managerRole;
    private OffsetDateTime heartbeatTime;
    private OffsetDateTime checkTime;
    private String uptime;
    private Double loadAverage1;
    private Double loadAverage5;
    private Double loadAverage15;
    private Double cpuUsage;
    private Double memoryUsage;
    private Double swapUsage;
    private Double diskUsage;

    private Boolean isActiveMonitoring;
    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

}
