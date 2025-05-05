package com.ttgint.library.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_parse_table")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ParseTable implements Serializable {

    @Id
    @SequenceGenerator(name = "t_parse_table_seq_id", sequenceName = "t_parse_table_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_parse_table_seq_id")
    private Long id;
    private Long flowId;
    private Long allTableId;

    private String schemaName;
    private String tableName;

    private String objectType;
    private String objectKey;
    private String objectKeyLookup;
    private String objectKeyDescription;

    private Integer dateColumnIndex;
    private String dateColumnName;
    private String resultFileDelimiter;

    private String nodeType;
    private String subNodeType;
    private String elementType;
    private String subElementType;
    private String itemType;
    private String subItemType;
    private String tableType;
    private String subTableType;
    private String networkType;
    private String subNetworkType;

    private String groupType;
    private String dataType;
    private String dataSource;
    private String tableGroup;
    private String dataGroup;

    private String loaderTarget;

    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

}