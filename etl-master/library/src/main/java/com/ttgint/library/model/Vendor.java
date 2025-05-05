package com.ttgint.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_vendor",
        uniqueConstraints = {
                @UniqueConstraint(name = "t_vendor_ukey_vendor_code", columnNames = {"vendorCode"})})
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class Vendor implements Serializable {

    @Id
    @SequenceGenerator(name = "t_vendor_seq_id", sequenceName = "t_vendor_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_vendor_seq_id")
    private Long id;
    private String vendorCode;
    private String vendorName;

    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

}