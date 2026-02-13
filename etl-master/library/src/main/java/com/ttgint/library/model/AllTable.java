package com.ttgint.library.model;

import com.ttgint.library.record.MetadataDefineAllTableReqRec;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_all_table")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class AllTable implements Serializable {

    @Id
    @SequenceGenerator(name = "t_all_table_seq_id", sequenceName = "t_all_table_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_all_table_seq_id")
    private Long id;
    private Long flowId;

    private String dbCatalog;
    private String schemaName;
    private String tableName;
    private String tablespaceName;
    private String tableNameAlias;
    private String tableNameLookup;
    private String tableDescription;

    private String objectKey;
    private String objectKey2;
    private String objectType;
    private String elementType;
    private String nodeType;
    private String itemType;
    private String tableType;
    private String subTableType;
    private String networkType;
    private String subNetworkType;

    private String dataType;
    private String dataInterval;
    private Long timePeriod;
    private Long timeDelay;

    private Boolean needRefresh;
    private Boolean isGenerated;
    private Boolean isFailed;
    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

    public AllTable(MetadataDefineAllTableReqRec record, int tableNameId) {
        this.flowId = record.getFlowId();
        this.dbCatalog = record.getDbCatalog();
        this.schemaName = record.getSchemaName() == null ? "pm" : record.getSchemaName();
        this.tableName = record.getTableName() != null ? record.getTableName() :
                "t" + String.format("%1$" + 5 + "s", record.getFlowId()).replace(' ', '0') +
                        "n" + String.format("%1$" + 6 + "s", tableNameId).replace(' ', '0');
        this.tablespaceName = record.getTablespaceName();
        this.tableNameAlias = record.getTableNameAlias() != null ? record.getTableNameAlias() :
                "t" + record.getFlowId() + "n" + tableNameId;
        this.tableNameLookup = record.getTableNameLookup();
        this.tableDescription = record.getTableDescription();

        this.objectKey = record.getObjectKey();
        this.objectKey2 = record.getObjectKey2();
        this.objectType = record.getObjectType();
        this.elementType = record.getElementType();
        this.nodeType = record.getNodeType();
        this.itemType = record.getItemType();
        this.tableType = record.getTableType();
        this.subTableType = record.getSubTableType();
        this.networkType = record.getNetworkType();
        this.subNetworkType = record.getSubNetworkType();

        this.dataType = record.getDataType();
        this.dataInterval = record.getDataInterval();
        this.timePeriod = record.getTimePeriod();
        this.timeDelay = record.getTimeDelay();

        this.needRefresh = true;
        this.isGenerated = false;
        this.isFailed = false;
        this.isActive = true;
        this.createdTime = OffsetDateTime.now();
    }


}