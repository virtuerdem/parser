package com.ttgint.library.record;

import com.ttgint.library.enums.ProgressType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.File;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class ParseHandlerRecord {

    private Long flowId;
    private String flowCode;
    private String flowProcessCode;

    private File file;
    private ProgressType progressType;

    private Boolean isActiveAutoCounter;
    private String resultFileExtension;
    private String rawPath;

    public static ParseHandlerRecord getRecord(ParseEngineRecord engineRecord,
                                               File file,
                                               ProgressType progressType) {
        ParseHandlerRecord record = new ParseHandlerRecord();
        record.setFlowId(engineRecord.getFlowId());
        record.setFlowCode(engineRecord.getFlowCode());
        record.setFlowProcessCode(engineRecord.getFlowProcessCode());

        record.setFile(file);
        record.setProgressType(progressType);

        record.setIsActiveAutoCounter(engineRecord.getIsActiveAutoCounter());
        record.setResultFileExtension(engineRecord.getResultFileExtension());
        record.setRawPath(engineRecord.getRawPath());

        return record;
    }

}
