package com.ttgint.library.connection;

import com.ttgint.library.enums.FileTimeFilter;
import com.ttgint.library.enums.PathWalkMethod;
import com.ttgint.library.model.TransferConnectionHistory;
import com.ttgint.library.record.ConnectionRecord;
import com.ttgint.library.record.RemoteFileRecord;
import com.ttgint.library.record.RemoteFileTransferRecord;
import com.ttgint.library.repository.TransferConnectionHistoryRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
public class TransferConnectionFactory {

    private final ConnectionRecord connectionRecord;
    private final TransferConnection transferConnection;
    private final TransferConnectionHistoryRepository transferConnectionHistoryRepository;
    private final TransferConnectionHistory transferConnectionHistory = new TransferConnectionHistory();

    public TransferConnectionFactory(ApplicationContext applicationContext, ConnectionRecord connectionRecord) {
        this.connectionRecord = connectionRecord;
        if (connectionRecord.getConProtocolType().getValue().equals("SFTP")) {
            this.transferConnection = new SFtpConnection(applicationContext, connectionRecord);
        } else if (connectionRecord.getConProtocolType().getValue().equals("FTP")) {
            this.transferConnection = new FtpConnection(applicationContext, connectionRecord);
        } else {
            this.transferConnection = new CpConnection(applicationContext, connectionRecord);
        }
        this.transferConnectionHistoryRepository = applicationContext.getBean(TransferConnectionHistoryRepository.class);
    }

    public Boolean connect() {
        boolean status = transferConnection.connect();
        prepareHistory(status);
        if (!status) {
            saveHistory();
        }
        return status;
    }

    public void disconnect() {
        saveHistory();
        transferConnection.disconnect();
    }

    public List<RemoteFileRecord> readFiles(String remotePath, FileTimeFilter filter, PathWalkMethod walkMethod) {
        transferConnectionHistory.setReadStartTime(OffsetDateTime.now());
        List<RemoteFileRecord> result;
        if (walkMethod.equals(PathWalkMethod.CURRENT)) {
            result = transferConnection.readFilesInCurrentPath(remotePath, filter);
        } else { // PathWalkMethod.NESTED
            result = transferConnection.readFilesInWalkingPath(remotePath, filter);
        }
        transferConnectionHistory.setReadEndTime(OffsetDateTime.now());
        transferConnectionHistory.setReadFileSize(result.stream().map(RemoteFileRecord::getFileSize).reduce(0L, Long::sum));
        transferConnectionHistory.setReadFileCount((long) result.size());
        return result;
    }

    public List<RemoteFileRecord> readFolders(String remotePath, FileTimeFilter filter, PathWalkMethod walkMethod) {
        List<RemoteFileRecord> result;
        if (walkMethod.equals(PathWalkMethod.CURRENT)) {
            result = transferConnection.readFoldersInCurrentPath(remotePath, filter);
        } else { // PathWalkMethod.NESTED
            result = transferConnection.readFoldersInWalkingPath(remotePath, filter);
        }
        return result;
    }

    public RemoteFileTransferRecord downloadFile(String remoteAbsolutePath, String localAbsolutePath) {
        if (transferConnectionHistory.getTransferStartTime() == null) {
            transferConnectionHistory.setTransferStartTime(OffsetDateTime.now());
        }
        RemoteFileTransferRecord record = transferConnection.downloadFile(remoteAbsolutePath, localAbsolutePath);
        transferConnectionHistory.setTransferEndTime(OffsetDateTime.now());
        if (record.getIsDownloaded()) {
            transferConnectionHistory.setTransferFileSize(transferConnectionHistory.getTransferFileSize() + record.getFileSize());
            transferConnectionHistory.setTransferFileCount(transferConnectionHistory.getTransferFileCount() + 1L);
        }
        return record;
    }

    public Boolean createFolder(String remotePath) {
        return transferConnection.createFolder(remotePath);
    }

    public Boolean uploadFile(String localAbsolutePath, String remoteAbsolutePath) {
        return transferConnection.uploadFile(localAbsolutePath, remoteAbsolutePath);
    }

    public Boolean deleteFile(String remoteAbsolutePath) {
        return transferConnection.deleteFile(remoteAbsolutePath);
    }

    public void prepareHistory(Boolean connectionStatus) {
        transferConnectionHistory.setFlowId(connectionRecord.getFlowId());
        transferConnectionHistory.setFlowProcessCode(connectionRecord.getFlowProcessCode());
        transferConnectionHistory.setConnectionId(connectionRecord.getConnectionId());
        transferConnectionHistory.setConnectionHistoryCode(connectionRecord.getConnectionHistoryCode());
        transferConnectionHistory.setConnectionStartTime(OffsetDateTime.now());
        transferConnectionHistory.setIsConnected(connectionStatus);
        transferConnectionHistory.setReadFileCount(0L);
        transferConnectionHistory.setReadFileSize(0L);
        transferConnectionHistory.setTransferFileCount(0L);
        transferConnectionHistory.setTransferFileSize(0L);
    }

    public void saveHistory() {
        transferConnectionHistory.setConnectionEndTime(OffsetDateTime.now());
        transferConnectionHistoryRepository.save(transferConnectionHistory);
    }

}
