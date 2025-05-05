package com.ttgint.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_archive_component",
        uniqueConstraints = {
                @UniqueConstraint(name = "t_archive_component_ukey_component_code", columnNames = {"componentCode"})})
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ArchiveComponent implements Serializable {

    @Id
    @SequenceGenerator(name = "t_archive_component_seq_id", sequenceName = "t_archive_component_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_archive_component_seq_id")
    private Long id;
    private String componentCode;

    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

}
