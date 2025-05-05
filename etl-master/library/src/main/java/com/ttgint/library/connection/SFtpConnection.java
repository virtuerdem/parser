package com.ttgint.library.connection;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpATTRS;
import com.ttgint.library.enums.FileInfo;
import com.ttgint.library.enums.FileTimeFilter;
import com.ttgint.library.record.ConnectionRecord;
import com.ttgint.library.record.RemoteFileRecord;
import com.ttgint.library.record.RemoteFileTransferRecord;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.provider.sftp.SftpClientFactory;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.springframework.context.ApplicationContext;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class SFtpConnection extends TransferConnection {

    private Session session;
    private ChannelSftp channelSftp;

    public SFtpConnection(ApplicationContext applicationContext, ConnectionRecord connectionRecord) {
        super(applicationContext, connectionRecord);
    }

    @Override
    public Boolean connect() {
        boolean status = false;
        try {
            session = SftpClientFactory.createConnection(
                    connectionRecord.getIp(),
                    connectionRecord.getPort(),
                    connectionRecord.getUserName().toCharArray(),
                    connectionRecord.getUserPass().toCharArray(),
                    createDefaultOptions());
            channelSftp = (ChannelSftp) session.openChannel("sftp");
            channelSftp.connect();
            status = true;
        } catch (Exception exception) {
            insertError("SFTP001-1", null, exception.getMessage());
        }
        return status;
    }

    @Override
    public void disconnect() {
        try {
            if (channelSftp != null && channelSftp.isConnected()) {
                channelSftp.disconnect();
            }

            if (session != null && session.isConnected()) {
                session.disconnect();
            }
        } catch (Exception exception) {
            insertError("SFTP002-1", null, exception.getMessage());
        }
    }

    @Override
    public List<RemoteFileRecord> readFilesInCurrentPath(String remotePath, FileTimeFilter filter) {
        try {
            return channelSftp.ls(remotePath)
                    .stream()
                    .parallel()
                    .filter(e -> !e.getAttrs().isDir())
                    .filter(e -> !e.getFilename().equals(".") && !e.getFilename().equals(".."))
                    .filter(e -> checkModifiedTime(e.getAttrs().getMTime() * 1000L) || filter.equals(FileTimeFilter.ALL))
                    .map(e -> {
                        try {
                            return RemoteFileRecord.getRecord(
                                            connectionRecord.getConnectionId(),
                                            remotePath,
                                            e.getFilename(),
                                            FileInfo.FILE,
                                            e.getAttrs().getSize(),
                                            convertTime(e.getAttrs().getMTime() * 1000L),
                                    (e.getAttrs().getMTime() * 1000L));
                        } catch (Exception exception) {
                            insertError("SFTP003-1", e.getFilename(), exception.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception exception) {
            insertError("SFTP003-2", remotePath, exception.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public List<RemoteFileRecord> readFoldersInCurrentPath(String remotePath, FileTimeFilter filter) {
        try {
            return channelSftp.ls(remotePath)
                    .stream()
                    .parallel()
                    .filter(e -> e.getAttrs().isDir())
                    .filter(e -> !e.getFilename().equals(".") && !e.getFilename().equals(".."))
                    .filter(e -> checkModifiedTime(e.getAttrs().getMTime() * 1000L) || filter.equals(FileTimeFilter.ALL))
                    .map(e -> {
                        try {
                            return RemoteFileRecord.getRecord(
                                            connectionRecord.getConnectionId(),
                                            remotePath,
                                            e.getFilename(),
                                            FileInfo.FOLDER,
                                            e.getAttrs().getSize(),
                                            convertTime(e.getAttrs().getMTime() * 1000L),
                                    (e.getAttrs().getMTime() * 1000L));
                        } catch (Exception exception) {
                            insertError("SFTP004-1", e.getFilename(), exception.getMessage());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .toList();
        } catch (Exception exception) {
            insertError("SFTP004-2", remotePath, exception.getMessage());
            return new ArrayList<>();
        }
    }

    @Override
    public RemoteFileTransferRecord downloadFile(String remoteAbsolutePath, String localAbsolutePath) {
        RemoteFileTransferRecord record = new RemoteFileTransferRecord(false);
        try (FileOutputStream fileOutputStream = new FileOutputStream(localAbsolutePath);
             BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {
            channelSftp.get(remoteAbsolutePath, bufferedOutputStream);
            bufferedOutputStream.flush();
            fileOutputStream.flush();

            record.setIsDownloaded(true);
            record.setFileTransferTime(OffsetDateTime.now());

            SftpATTRS sftpATTRS = channelSftp.stat(remoteAbsolutePath);
            record.setFileSize(sftpATTRS.getSize());
            record.setFileModifiedTimeStr(sftpATTRS.getMTime() * 1000L);
            record.setFileModifiedTime(convertTime(sftpATTRS.getMTime() * 1000L));
        } catch (Exception exception) {
            insertError("SFTP005-1", remoteAbsolutePath, exception.getMessage());
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
            channelSftp.rm(remoteAbsolutePath);
            return true;
        } catch (Exception exception) {
            insertError("SFTP008-1", remoteAbsolutePath, exception.getMessage());
            return false;
        }
    }

    private FileSystemOptions createDefaultOptions() throws FileSystemException {
        FileSystemOptions opts = new FileSystemOptions();
        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
        SftpFileSystemConfigBuilder.getInstance().setConnectTimeoutMillis(opts, 10000);
        SftpFileSystemConfigBuilder.getInstance().setPreferredAuthentications(opts, "publickey,keyboard-interactive,password");
        return opts;
    }

}
