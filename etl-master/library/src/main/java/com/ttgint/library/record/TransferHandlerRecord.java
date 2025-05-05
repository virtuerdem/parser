package com.ttgint.library.record;

import com.ttgint.library.model.Connection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class TransferHandlerRecord {

    private Long flowId;
    private String flowCode;
    private String flowProcessCode;
    private ConnectionRecord connectionRecord;

    private Boolean preThread;
    private Boolean checkLastModifiedTime;
    private Boolean readFiles;
    private Boolean filterFiles;
    private Boolean setFileInfo;
    private Boolean cacheResults;
    private Boolean setLastModifiedTime;
    private Boolean clearRemoteFiles;
    private Boolean download;
    private Boolean postThread;

    private String rawPath;


    public static TransferHandlerRecord getRecord(TransferEngineRecord engineRecord, Connection connection) {
        TransferHandlerRecord record = new TransferHandlerRecord();
        record.setFlowId(engineRecord.getFlowId());
        record.setFlowCode(engineRecord.getFlowCode());
        record.setFlowProcessCode(engineRecord.getFlowProcessCode());
        record.setConnectionRecord(ConnectionRecord.getRecord(connection, engineRecord.getFlowCode(), engineRecord.getFlowProcessCode()));

        record.setPreThread(engineRecord.getPreThread());
        record.setCheckLastModifiedTime(engineRecord.getCheckLastModifiedTime());
        record.setReadFiles(engineRecord.getReadFiles());
        record.setFilterFiles(engineRecord.getFilterFiles());
        record.setSetFileInfo(engineRecord.getSetFileInfo());
        record.setCacheResults(engineRecord.getCacheResults());
        record.setSetLastModifiedTime(engineRecord.getSetLastModifiedTime());
        record.setClearRemoteFiles(engineRecord.getClearRemoteFiles());
        record.setDownload(engineRecord.getDownload());
        record.setPostThread(engineRecord.getPostThread());

        record.setRawPath(engineRecord.getRawPath());

        return record;
    }
}
