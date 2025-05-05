package com.ttgint.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_manager_monitoring")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ManagerMonitoring implements Serializable {

    @Id
    @SequenceGenerator(name = "t_manager_monitoring_seq_id", sequenceName = "t_manager_monitoring_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_manager_monitoring_seq_id")
    private Long id;
    private Long managerId;
    private OffsetDateTime checkTime;
    private String uptime;
    private Double loadAverage;
    private Double loadAverage1;
    private Double loadAverage5;
    private Double loadAverage15;
    private Double cpuUsage;
    private Double memoryUsage;
    private Double swapUsage;
    private Double diskUsage;

}
