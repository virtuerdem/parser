package com.ttgint.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_network_item")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class NetworkItem implements Serializable {

    @Id
    @SequenceGenerator(name = "t_network_item_seq_id", sequenceName = "t_network_item_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_network_item_seq_id")
    private Long id;
    private Long unitId;
    private Long branchId;
    private Long flowId;
    private Long itemId;
    private Long itemParentId;
    private String itemCode;
    private String itemName;
    private String itemNameLookup;
    private String itemIp;
    private String itemPort;
    private String itemVersion;
    private String itemType;
    private String subNetwork;
    private String fileFormatVersion;
    private String vendorName;
    private String dc;
    private String fileType;
    private String itemManager;
    private String itemManagerName;

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