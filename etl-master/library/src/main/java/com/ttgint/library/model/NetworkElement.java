package com.ttgint.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_network_element")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class NetworkElement implements Serializable {

    @Id
    @SequenceGenerator(name = "t_network_element_seq_id", sequenceName = "t_network_element_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_network_element_seq_id")
    private Long id;
    private Long unitId;
    private Long branchId;
    private Long flowId;
    private Long elementId;
    private Long elementParentId;
    private String elementCode;
    private String elementName;
    private String elementNameLookup;
    private String elementIp;
    private String elementPort;
    private String elementVersion;
    private String elementType;
    private String subNetwork;
    private String fileFormatVersion;
    private String vendorName;
    private String dc;
    private String fileType;
    private String elementManager;
    private String elementManagerName;

    private String sourceIp;
    private Long sourceConnectionId;
    private String sourcePath;
    private String sourceFileFormat;
    private Integer sourceTimePeriod;
    private String sourceStartsWith;
    private String sourceEndsWith;
    private OffsetDateTime sourceLastModifiedTime;

    private String primarySourceIp;
    private Long primarySourceConnectionId;
    private String primarySourcePath;
    private String primarySourceFileFormat;
    private Integer primarySourceTimePeriod;
    private String primarySourceStartsWith;
    private String primarySourceEndsWith;
    private OffsetDateTime primarySourceLastModifiedTime;

    private String secondarySourceIp;
    private Long secondarySourceConnectionId;
    private String secondarySourcePath;
    private String secondarySourceFileFormat;
    private Integer secondarySourceTimePeriod;
    private String secondarySourceStartsWith;
    private String secondarySourceEndsWith;
    private OffsetDateTime secondarySourceLastModifiedTime;

    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

}