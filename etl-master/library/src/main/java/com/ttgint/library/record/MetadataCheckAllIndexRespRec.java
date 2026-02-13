package com.ttgint.library.record;

import com.ttgint.library.model.AllIndex;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class MetadataCheckAllIndexRespRec {

    private Long flowId;

    private Long allTableId;
    private String schemaName;
    private String tableName;

    private String indexName;
    private String indexColumnName;
    private String indexTableSpace;

    private Boolean isActive;

    public MetadataCheckAllIndexRespRec(AllIndex index) {
        this.flowId = index.getFlowId();
        this.allTableId = index.getAllTableId();
        this.schemaName = index.getSchemaName();
        this.tableName = index.getTableName();
        this.indexName = index.getIndexName();
        this.indexColumnName = index.getIndexColumnName();
        this.indexTableSpace = index.getIndexTableSpace();
        this.isActive = index.getIsActive();
    }
}
