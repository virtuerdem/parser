package com.ttgint.library.connection;

import com.ttgint.library.enums.FileInfo;
import com.ttgint.library.enums.FileTimeFilter;
import com.ttgint.library.record.ConnectionRecord;
import com.ttgint.library.record.RemoteFileRecord;
import com.ttgint.library.record.RemoteFileTransferRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
public class CpConnection extends TransferConnection {

    public CpConnection(ApplicationContext applicationContext, ConnectionRecord connectionRecord) {
        super(applicationContext, connectionRecord);
    }

    @Override
    public Boolean connect() {
        boolean status = false;
        try {
            status = new File(connectionRecord.getRemotePath()).exists();
            if (!status) {
                throw new Exception("failed");
            }
        } catch (Exception exception) {
            insertError("CP001-1", null, exception.getMessage());
        }
        return status;
    }

    @Override
    public void disconnect() {
    }

    @Override
    public List<RemoteFileRecord> readFilesInCurrentPath(String remotePath, FileTimeFilter filter) {
        try {
            return Arrays.stream(new File(remotePath).listFiles())
                    .parallel()
                    .filter(File::isFile)
                    .filter(e -> !e.getName().equals(".") && !e.getName().equals(".."))
                    .filter(e -> checkModifiedTime(e.lastModified()) || filter.equals(FileTimeFilter.ALL))
                    .map(e -> {
                        try {
                            return RemoteFileRecord.getRecord(
                                            connectionRecord.getConnectionId(),
                                            remotePath,
                                            e.getName(),
                                            FileInfo.FILE,
                                            e.length(),
                                            convertTime(e.lastModified()),
                                    e.lastModified());
                        } catch (Exception exception) {
                            insertError("CP003-1", e.getName(), exception.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception exception) {
            insertError("CP003-2", remotePath, exception.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<RemoteFileRecord> readFoldersInCurrentPath(String remotePath, FileTimeFilter filter) {
        try {
            return Arrays.stream(new File(remotePath).listFiles())
                    .parallel()
                    .filter(File::isDirectory)
                    .filter(e -> !e.getName().equals(".") && !e.getName().equals(".."))
                    .filter(e -> checkModifiedTime(e.lastModified()) || filter.equals(FileTimeFilter.ALL))
                    .map(e -> {
                        try {
                            return RemoteFileRecord.getRecord(
                                            connectionRecord.getConnectionId(),
                                            remotePath,
                                            e.getName(),
                                            FileInfo.FOLDER,
                                            e.length(),
                                            convertTime(e.lastModified()),
                                    e.lastModified());
                        } catch (Exception exception) {
                            insertError("CP004-1", e.getName(), exception.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception exception) {
            insertError("CP004-2", remotePath, exception.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public RemoteFileTransferRecord downloadFile(String remoteAbsolutePath, String localAbsolutePath) {
        RemoteFileTransferRecord record = new RemoteFileTransferRecord(false);
        try {
            Files.copy(Paths.get(remoteAbsolutePath), Paths.get(localAbsolutePath),
                    StandardCopyOption.REPLACE_EXISTING);

            record.setIsDownloaded(true);
            record.setFileTransferTime(OffsetDateTime.now());

            File file = new File(localAbsolutePath);
            record.setFileSize(file.length());
            record.setFileModifiedTimeStr(file.lastModified());
            record.setFileModifiedTime(convertTime(file.lastModified()));
        } catch (Exception exception) {
            insertError("CP005-1", remoteAbsolutePath, exception.getMessage());
            deleteLocalFile(localAbsolutePath);
        }
        return record;
    }

    @Override
    public Boolean createFolder(String remotePath) {
        return false;
    }

    @Override
    public Boolean uploadFile(String localAbsolutePath, String remoteAbsolutePath) {
        return false;
    }

    @Override
    public Boolean deleteFile(String remoteAbsolutePath) {
        try {
            Files.delete(Paths.get(remoteAbsolutePath));
            return true;
        } catch (Exception exception) {
            insertError("CP008-1", remoteAbsolutePath, exception.getMessage());
            return false;
        }
    }

}
