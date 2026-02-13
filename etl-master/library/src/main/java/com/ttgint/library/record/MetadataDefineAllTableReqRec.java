package com.ttgint.library.record;

import com.ttgint.library.model.AllCounter;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class MetadataDefineAllTableReqRec {

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

    public MetadataDefineAllTableReqRec(AllCounter record, Long timePeriod) {
        this.flowId = record.getFlowId();
        this.elementType = record.getElementType();
        this.objectType = record.getCounterGroupType();
        this.objectKey = record.getCounterGroupKey();
        this.timePeriod = timePeriod;
    }

}
