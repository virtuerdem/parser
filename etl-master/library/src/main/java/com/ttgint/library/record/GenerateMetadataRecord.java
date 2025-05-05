package com.ttgint.library.record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class GenerateMetadataRecord {

    private Long flowId;
    private String flowCode;
    private String flowProcessCode;

    public static GenerateMetadataRecord getRecord(ParseEngineRecord engine) {
        GenerateMetadataRecord record = new GenerateMetadataRecord();
        record.setFlowId(engine.getFlowId());
        record.setFlowCode(engine.getFlowCode());
        record.setFlowProcessCode(engine.getFlowProcessCode());

        return record;
    }

}

