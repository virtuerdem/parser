package com.ttgint.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_branch",
        uniqueConstraints = {
                @UniqueConstraint(name = "t_branch_ukey_branch_code", columnNames = {"branchCode"})})
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Branch implements Serializable {

    @Id
    @SequenceGenerator(name = "t_branch_seq_id", sequenceName = "t_branch_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_branch_seq_id")
    private Long id;
    private Long companyId;
    private Long domainId;
    private Long organisationId;
    private Long vendorId;
    private Long unitId;
    private String branchCode;
    private String branchInfo;

    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

}
