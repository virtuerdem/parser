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

    private String sourceStartWith;
    private String sourceContains;
    private String sourceEndWith;

    public static DecompressRecord getRecord(Long flowId,
                                             String flowProcessCode,
                                             String targetPath,
                                             Boolean needResult,
                                             File sourceFile,
                                             OffsetDateTime fragmentTime,
                                             String fileId,
                                             String fileNamePrefix,
                                             String targetFileName) {
        DecompressRecord record = new DecompressRecord();
        record.setFlowId(flowId);
        record.setFlowProcessCode(flowProcessCode);
        record.setTargetPath(targetPath);
        record.setNeedResult(needResult);
        record.setSourceFile(sourceFile);
        record.setFragmentTime(fragmentTime);
        record.setFileId(fileId);
        record.setFileNamePrefix(fileNamePrefix);
        record.setTargetFileName(targetFileName);
        return record;
    }

    public static DecompressRecord getRecord(TransferEngineRecord engineRecord,
                                             File sourceFile,
                                             OffsetDateTime fragmentTime,
                                             String fileId,
                                             String fileNamePrefix,
                                             String targetFileName) {
        return getRecord(
                engineRecord.getFlowId(),
                engineRecord.getFlowProcessCode(),
                engineRecord.getRawPath(),
                engineRecord.getNeedDecompressResult(),
                sourceFile,
                fragmentTime,
                fileId,
                fileNamePrefix,
                targetFileName);
    }

    public static DecompressRecord getRecord(ParseEngineRecord engineRecord,
                                             File sourceFile,
                                             OffsetDateTime fragmentTime,
                                             String fileId,
                                             String fileNamePrefix,
                                             String targetFileName) {
        return getRecord(
                engineRecord.getFlowId(),
                engineRecord.getFlowProcessCode(),
                engineRecord.getRawPath(),
                engineRecord.getNeedDecompressResult(),
                sourceFile,
                fragmentTime,
                fileId,
                fileNamePrefix,
                targetFileName);
    }

    public static DecompressRecord getRecord(TransferEngineRecord engineRecord,
                                             File sourceFile,
                                             OffsetDateTime fragmentTime,
                                             String fileId,
                                             String fileNamePrefix,
                                             String targetFileName,
                                             String sourceStartWith,
                                             String sourceContains,
                                             String sourceEndWith) {
        DecompressRecord record = getRecord(
                engineRecord.getFlowId(),
                engineRecord.getFlowProcessCode(),
                engineRecord.getRawPath(),
                engineRecord.getNeedDecompressResult(),
                sourceFile,
                fragmentTime,
                fileId,
                fileNamePrefix,
                targetFileName);
        record.setSourceStartWith(sourceStartWith);
        record.setSourceContains(sourceContains);
        record.setSourceEndWith(sourceEndWith);
        return record;
    }

}
