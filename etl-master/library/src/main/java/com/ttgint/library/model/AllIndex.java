package com.ttgint.library.model;

import com.ttgint.library.record.MetadataDefineAllIndexReqRec;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;

@Table(name = "t_all_index")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class AllIndex implements Serializable {

    @Id
    @SequenceGenerator(name = "t_all_index_seq_id", sequenceName = "t_all_index_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_all_index_seq_id")
    private Long id;
    private Long flowId;

    private Long allTableId;
    private String schemaName;
    private String tableName;
    private String indexName;
    private String indexColumnName;
    private String indexTableSpace;

    private Boolean needRefresh;
    private Boolean isGenerated;
    private Boolean isFailed;
    private Boolean isActive;
    private OffsetDateTime createdTime;
    private String createdBy;
    private OffsetDateTime updatedTime;
    private String updatedBy;
    private String extraInfo;

    public AllIndex(Long flowId,
                    Long allTableId,
                    String schemaName,
                    String tableName,
                    MetadataDefineAllIndexReqRec record) {
        this.flowId = flowId;
        this.allTableId = allTableId;
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.indexName = record.getIndexName() != null ? record.getIndexName() : tableName + "_idx_" + record.getIndexColumnName();
        this.indexColumnName = record.getIndexColumnName();
        this.indexTableSpace = record.getIndexTableSpace() == null ? schemaName : record.getIndexTableSpace();

        this.needRefresh = true;
        this.isGenerated = false;
        this.isFailed = false;
        this.isActive = true;
        this.createdTime = OffsetDateTime.now();
    }

}
