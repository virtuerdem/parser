package com.ttgint.library.record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.File;
import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class DecompressRecord {

    private Long flowId;
    private String flowProcessCode;
    private File sourceFile;
    private OffsetDateTime fragmentTime;
    private String targetPath;
    private String fileId;
    private String fileNamePrefix;
    private String targetFileName;
    private Boolean needResult;

    public static DecompressRecord getRecord(TransferEngineRecord engineRecord,
                                             File sourceFile,
                                             OffsetDateTime fragmentTime,
                                             String fileId,
                                             String fileNamePrefix,
                                             String targetFileName) {
        DecompressRecord record = new DecompressRecord();
        record.setFlowId(engineRecord.getFlowId());
        record.setFlowProcessCode(engineRecord.getFlowProcessCode());
        record.setSourceFile(sourceFile);
        record.setFragmentTime(fragmentTime);
        record.setTargetPath(engineRecord.getRawPath());
        record.setFileId(fileId);
        record.setFileNamePrefix(fileNamePrefix);
        record.setTargetFileName(targetFileName);
        record.setNeedResult(engineRecord.getNeedDecompressResult());

        return record;
    }

}
