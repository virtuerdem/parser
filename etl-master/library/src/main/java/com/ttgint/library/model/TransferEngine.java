package com.ttgint.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_transfer_engine")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class TransferEngine implements Serializable {

    @Id
    @SequenceGenerator(name = "t_transfer_engine_seq_id", sequenceName = "t_transfer_engine_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_transfer_engine_seq_id")
    private Long id;
    private Long flowId;
    private Long transferComponentId;

    private Boolean preTransfer;
    private Integer preTransferThreadCount;
    private Boolean onTransfer;
    private Integer onTransferThreadCount;

    private Boolean preThread;
    private Boolean checkLastModifiedTime;
    private Boolean readFiles;
    private Boolean filterFiles;
    private Boolean setFileInfo;
    private Boolean cacheResults;
    private Boolean setLastModifiedTime;
    private Boolean clearRemoteFiles;
    private Boolean download;
    private Boolean postThread;

    private Boolean decompress;
    private Integer decompressThreadCount;
    private Boolean needDecompressResult;
    private Boolean validation;
    private Integer validationThreadCount;
    private Boolean postTransfer;
    private Integer postTransferThreadCount;

    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

}