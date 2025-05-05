package com.ttgint.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_company",
        uniqueConstraints = {
                @UniqueConstraint(name = "t_company_ukey_company_code", columnNames = {"companyCode"})})
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Company implements Serializable {

    @Id
    @SequenceGenerator(name = "t_company_seq_id", sequenceName = "t_company_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_company_seq_id")
    private Long id;
    private String companyCode;
    private String companyName;
    private String companyRegion;

    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

}
