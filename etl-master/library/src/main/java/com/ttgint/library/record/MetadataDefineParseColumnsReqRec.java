package com.ttgint.library.record;

import com.ttgint.library.model.AllTable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class MetadataDefineParseColumnsReqRec {

    private Long flowId;
    private Long parseTableId;
    private String schemaName;
    private String tableName;
    private List<MetadataDefineParseColumnReqRec> columns;

    public MetadataDefineParseColumnsReqRec(AllTable table) {
        this.flowId = table.getFlowId();
        this.schemaName = table.getSchemaName();
        this.tableName = table.getTableName();
    }

}
