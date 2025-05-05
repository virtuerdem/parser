package com.ttgint.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_organisation",
        uniqueConstraints = {
                @UniqueConstraint(name = "t_organisation_ukey_organisation_code", columnNames = {"organisationCode"})})
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Organisation implements Serializable {

    @Id
    @SequenceGenerator(name = "t_organisation_seq_id", sequenceName = "t_organisation_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_organisation_seq_id")
    private Long id;
    private String organisationCode;
    private String organisationName;
    private String organisationRegion;

    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

}