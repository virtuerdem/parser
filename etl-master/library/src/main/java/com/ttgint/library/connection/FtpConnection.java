package com.ttgint.library.connection;

import com.ttgint.library.enums.FileInfo;
import com.ttgint.library.enums.FileTimeFilter;
import com.ttgint.library.record.ConnectionRecord;
import com.ttgint.library.record.RemoteFileRecord;
import com.ttgint.library.record.RemoteFileTransferRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.context.ApplicationContext;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Slf4j
public class FtpConnection extends TransferConnection {

    private FTPClient ftpClient;

    public FtpConnection(ApplicationContext applicationContext, ConnectionRecord connectionRecord) {
        super(applicationContext, connectionRecord);
    }

    @Override
    public Boolean connect() {
        boolean status = false;
        try {
            ftpClient = new FTPClient();
            ftpClient.connect(connectionRecord.getIp(), connectionRecord.getPort());
            ftpClient.enterLocalPassiveMode();
            ftpClient.setConnectTimeout(5000);
            ftpClient.setControlKeepAliveTimeout(720);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            status = ftpClient.login(connectionRecord.getUserName(), connectionRecord.getUserPass());
            if (!status) {
                throw new Exception("failed");
            }
        } catch (Exception exception) {
            insertError("FTP001-1", null, exception.getMessage());
        }
        return status;
    }

    @Override
    public void disconnect() {
        try {
            if (ftpClient != null && ftpClient.isConnected()) {
                ftpClient.logout();
                ftpClient.disconnect();
            }
        } catch (Exception exception) {
            insertError("FTP002-1", null, exception.getMessage());
        }
    }

    @Override
    public List<RemoteFileRecord> readFilesInCurrentPath(String remotePath, FileTimeFilter filter) {
        try {
            return Arrays.stream(ftpClient.listFiles(remotePath))
                    .parallel()
                    .filter(FTPFile::isFile)
                    .filter(e -> !e.getName().equals(".") && !e.getName().equals(".."))
                    .filter(e -> checkModifiedTime(e.getTimestamp().getTimeInMillis()) || filter.equals(FileTimeFilter.ALL))
                    .map(e -> {
                        try {
                            return RemoteFileRecord.getRecord(
                                            connectionRecord.getConnectionId(),
                                            remotePath,
                                            e.getName(),
                                            FileInfo.FILE,
                                            e.getSize(),
                                            convertTime(e.getTimestamp().getTimeInMillis()),
                                    e.getTimestamp().getTimeInMillis());
                        } catch (Exception exception) {
                            insertError("FTP003-1", e.getName(), exception.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception exception) {
            insertError("FTP003-2", remotePath, exception.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<RemoteFileRecord> readFoldersInCurrentPath(String remotePath, FileTimeFilter filter) {
        try {
            return Arrays.stream(ftpClient.listFiles(remotePath))
                    .parallel()
                    .filter(FTPFile::isDirectory)
                    .filter(e -> !e.getName().equals(".") && !e.getName().equals(".."))
                    .filter(e -> checkModifiedTime(e.getTimestamp().getTimeInMillis()) || filter.equals(FileTimeFilter.ALL))
                    .map(e -> {
                        try {
                            return RemoteFileRecord.getRecord(
                                            connectionRecord.getConnectionId(),
                                            remotePath,
                                            e.getName(),
                                            FileInfo.FOLDER,
                                            e.getSize(),
                                            convertTime(e.getTimestamp().getTimeInMillis()),
                                    e.getTimestamp().getTimeInMillis());
                        } catch (Exception exception) {
                            insertError("FTP004-1", e.getName(), exception.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception exception) {
            insertError("FTP004-2", remotePath, exception.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public RemoteFileTransferRecord downloadFile(String remoteAbsolutePath, String localAbsolutePath) {
        RemoteFileTransferRecord record = new RemoteFileTransferRecord(false);
        try (FileOutputStream fileOutputStream = new FileOutputStream(localAbsolutePath);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {
            boolean status = ftpClient.retrieveFile(remoteAbsolutePath, bufferedOutputStream);
            bufferedOutputStream.flush();
            fileOutputStream.flush();

            if (status) {
                record.setIsDownloaded(true);
                record.setFileTransferTime(OffsetDateTime.now());

                FTPFile ftpFile = ftpClient.listFiles(remoteAbsolutePath)[0];
                record.setFileSize(ftpFile.getSize());
                record.setFileModifiedTimeStr(ftpFile.getTimestamp().getTimeInMillis());
                record.setFileModifiedTime(convertTime(ftpFile.getTimestamp().getTimeInMillis()));
            }
        } catch (Exception exception) {
            insertError("FTP005-1", remoteAbsolutePath, exception.getMessage());
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
        return false;
    }

}
