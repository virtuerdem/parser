package com.ttgint.library.model;

import com.ttgint.library.record.MetadataDefineParseTableReqRec;
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

    private String objectKey;
    private String objectType;
    private String elementType;
    private String nodeType;
    private String itemType;
    private String tableType;
    private String subTableType;
    private String networkType;
    private String subNetworkType;

    private String groupType;
    private String dataType;
    private String dataSource;
    private String tableGroup;
    private String dataGroup;

    private Integer dateColumnIndex;
    private String dateColumnName;
    private String resultFileDelimiter;
    private String loaderTarget;

    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

    public ParseTable(MetadataDefineParseTableReqRec record) {
        this.flowId = record.getFlowId();
        this.allTableId = record.getAllTableId();
        this.schemaName = record.getSchemaName();
        this.tableName = record.getTableName();

        this.objectKey = record.getObjectKey();
        this.objectType = record.getObjectType();
        this.elementType = record.getElementType();
        this.nodeType = record.getNodeType();
        this.itemType = record.getItemType();
        this.tableType = record.getTableType();
        this.subTableType = record.getSubTableType();
        this.networkType = record.getNetworkType();
        this.subNetworkType = record.getSubNetworkType();

        this.groupType = record.getGroupType();
        this.dataType = record.getDataType();
        this.dataSource = record.getDataSource();
        this.tableGroup = record.getTableGroup();
        this.dataGroup = record.getDataGroup();

        this.dateColumnIndex = record.getDateColumnIndex();
        this.dateColumnName = record.getDateColumnName();
        this.resultFileDelimiter = record.getResultFileDelimiter();
        this.loaderTarget = record.getLoaderTarget();

        this.isActive = true;
        this.createdTime = OffsetDateTime.now();
    }

}