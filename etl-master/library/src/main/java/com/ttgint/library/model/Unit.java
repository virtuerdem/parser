package com.ttgint.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_unit",
        uniqueConstraints = {
                @UniqueConstraint(name = "t_unit_ukey_unit_code", columnNames = {"unitCode"})})
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Unit implements Serializable {

    @Id
    @SequenceGenerator(name = "t_unit_seq_id", sequenceName = "t_unit_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_unit_seq_id")
    private Long id;
    private String unitCode;
    private String unitName;
    private String unitType;
    private String unitInfo;

    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

}
