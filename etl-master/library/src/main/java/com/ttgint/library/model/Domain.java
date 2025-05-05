package com.ttgint.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_domain",
        uniqueConstraints = {
                @UniqueConstraint(name = "t_domain_ukey_domain_code", columnNames = {"domainCode"})})
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Domain implements Serializable {

    @Id
    @SequenceGenerator(name = "t_domain_seq_id", sequenceName = "t_domain_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_domain_seq_id")
    private Long id;
    private String domainCode;
    private String domainName;
    private String domainRegion;

    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

}