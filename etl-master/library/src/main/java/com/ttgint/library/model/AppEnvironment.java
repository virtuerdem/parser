package com.ttgint.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_app_environment")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class AppEnvironment implements Serializable {

    @Id
    @SequenceGenerator(name = "t_app_environment_seq_id", sequenceName = "t_app_environment_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_app_environment_seq_id")
    private Long id;
    private Long masterId;
    private Long flowId;
    private String environmentCode;
    private String environmentKey;
    private String environmentValue;

    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

}
