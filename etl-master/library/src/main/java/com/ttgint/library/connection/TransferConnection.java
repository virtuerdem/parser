package com.ttgint.library.connection;

import com.ttgint.library.enums.FileTimeFilter;
import com.ttgint.library.model.ConnectionError;
import com.ttgint.library.record.ConnectionRecord;
import com.ttgint.library.record.RemoteFileRecord;
import com.ttgint.library.record.RemoteFileTransferRecord;
import com.ttgint.library.repository.ConnectionErrorRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
public abstract class TransferConnection {

    protected final ConnectionRecord connectionRecord;
    protected final ConnectionErrorRepository connectionErrorRepository;


    public TransferConnection(ApplicationContext applicationContext, ConnectionRecord connectionRecord) {
        this.connectionRecord = connectionRecord;
        this.connectionErrorRepository = applicationContext.getBean(ConnectionErrorRepository.class);
    }

    public abstract Boolean connect();

    public abstract void disconnect();

    public abstract List<RemoteFileRecord> readFilesInCurrentPath(String remotePath, FileTimeFilter filter);

    public List<RemoteFileRecord> readFilesInWalkingPath(String remotePath, FileTimeFilter filter) {
        List<RemoteFileRecord> transferFileRecords = new ArrayList<>();
        transferFileRecords.addAll(readFilesInCurrentPath(remotePath, filter));
        for (RemoteFileRecord folder : readFoldersInCurrentPath(remotePath, FileTimeFilter.ALL)) {
            transferFileRecords.addAll(readFilesInWalkingPath(folder.getAbsolutePath(), filter));
        }
        return transferFileRecords;
    }

    public abstract List<RemoteFileRecord> readFoldersInCurrentPath(String remotePath, FileTimeFilter filter);

    public List<RemoteFileRecord> readFoldersInWalkingPath(String remotePath, FileTimeFilter filter) {
        List<RemoteFileRecord> transferFileRecords = new ArrayList<>();
        transferFileRecords.addAll(readFoldersInCurrentPath(remotePath, filter));
        for (RemoteFileRecord folder : readFoldersInCurrentPath(remotePath, FileTimeFilter.ALL)) {
            transferFileRecords.addAll(readFoldersInWalkingPath(folder.getAbsolutePath(), filter));
        }
        return transferFileRecords;
    }

    public abstract RemoteFileTransferRecord downloadFile(String remoteAbsolutePath, String localAbsolutePath);

    public abstract Boolean createFolder(String remotePath);

    public abstract Boolean uploadFile(String localAbsolutePath, String remoteAbsolutePath);

    public abstract Boolean deleteFile(String remoteAbsolutePath);

    public void deleteLocalFile(String localAbsolutePath) {
        try {
            Files.deleteIfExists(Paths.get(localAbsolutePath));
        } catch (Exception ignored) {
        }
    }

    public OffsetDateTime convertTime(long time) {
        return new Date(time).toInstant().atZone(ZoneId.systemDefault()).toOffsetDateTime();
    }

    public Boolean checkModifiedTime(long time) {
        return convertTime(time).isAfter(connectionRecord.getLastModifiedTime());
    }

    protected void insertError(String errorCode, String errorSource, String errorMessage) {
        log.error("! TransferConnection errorCode:{} ip:{} source:{} message:{}",
                errorCode, connectionRecord.getIp(), errorSource, errorMessage);
        connectionErrorRepository.save(ConnectionError.getEntity(connectionRecord, errorCode, errorSource, errorMessage));
    }

}
