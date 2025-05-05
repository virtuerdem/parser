package com.ttgint.transfer.base;

import com.ttgint.library.connection.TransferConnectionFactory;
import com.ttgint.library.enums.FileTimeFilter;
import com.ttgint.library.model.Connection;
import com.ttgint.library.model.TransferConnectionResult;
import com.ttgint.library.record.RemoteFileRecord;
import com.ttgint.library.record.RemoteFileTransferRecord;
import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.library.repository.ConnectionRepository;
import com.ttgint.library.repository.TransferConnectionResultRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Getter
@Slf4j
public class TransferBaseHandler implements TransferHandler, Runnable {

    private final ApplicationContext applicationContext;
    private final TransferHandlerRecord handlerRecord;
    private final TransferConnectionFactory transferConnectionFactory;
    private final TransferConnectionResultRepository transferConnectionResultRepository;
    private final ConnectionRepository connectionRepository;

    private List<RemoteFileRecord> remoteFiles = new ArrayList<>();

    public TransferBaseHandler(ApplicationContext applicationContext, TransferHandlerRecord handlerRecord) {
        this.applicationContext = applicationContext;
        this.handlerRecord = handlerRecord;
        this.transferConnectionFactory = new TransferConnectionFactory(applicationContext, handlerRecord.getConnectionRecord());
        this.transferConnectionResultRepository = applicationContext.getBean(TransferConnectionResultRepository.class);
        this.connectionRepository = applicationContext.getBean(ConnectionRepository.class);
    }

    @Override
    public void run() {
        log.info("* TransferBaseHandler run");
        if (startConnection()) {
            if (handlerRecord.getPreThread()) {
                preHandler();
            }
            if (handlerRecord.getCheckLastModifiedTime()) {
                checkLastModifiedTime();
            }
            if (handlerRecord.getReadFiles()) {
                readFiles();
            }
            if (handlerRecord.getFilterFiles()) {
                filterFiles(); // mandatory Override at the extended Handler for MODIFIED or ALL TransferWorkType
            }
            if (handlerRecord.getSetFileInfo()) {
                setFileInfo(); // mandatory Override at the extended Handler for MODIFIED or ALL TransferWorkType
            }
            if (handlerRecord.getCacheResults()) {
                cacheResults();
            }
            if (handlerRecord.getSetLastModifiedTime()) {
                setLastModifiedTime();
            }
            if (handlerRecord.getClearRemoteFiles()) {
                clearRemoteFiles();
            }
            if (handlerRecord.getDownload()) {
                download();
            }
            if (handlerRecord.getPostThread()) {
                postHandler();
            }
            closeConnection();
        }
    }

    @Override
    public boolean startConnection() {
        log.info("* TransferBaseHandler startConnection");
        return transferConnectionFactory.connect();
    }

    @Override
    public void preHandler() {
        log.info("* TransferBaseHandler preHandler");
    }

    @Override
    public void checkLastModifiedTime() {
        log.info("* TransferBaseHandler checkLastModifiedTime");
        OffsetDateTime maxModifiedTime
                = transferConnectionResultRepository.getMaxModifiedTime(handlerRecord.getConnectionRecord().getConnectionId());
        if (maxModifiedTime != null
                && maxModifiedTime.isAfter(handlerRecord.getConnectionRecord().getLastModifiedTime())) {
            handlerRecord.getConnectionRecord().setLastModifiedTime(maxModifiedTime);
            setLastModifiedTime(maxModifiedTime);
        }
    }

    @Override
    public void readFiles() {
        log.info("* TransferBaseHandler listFiles");
        OffsetDateTime readStartTime
                = new Date().toInstant().atZone(ZoneId.systemDefault())
                .toOffsetDateTime().truncatedTo(ChronoUnit.SECONDS);

        List<RemoteFileRecord> remoteFilesTemp = new ArrayList<>(
                transferConnectionFactory.readFiles(
                        handlerRecord.getConnectionRecord().getRemotePath(),
                        handlerRecord.getConnectionRecord().getReadFileTimeFilter(),
                        handlerRecord.getConnectionRecord().getPathWalkMethod()
                )
        );

        // ignore files in the last modified second due to millisecond issue
        try {
            OffsetDateTime latestModifiedSecond
                    = remoteFilesTemp.parallelStream().map(RemoteFileRecord::getFileModifiedTime)
                    .max(OffsetDateTime::compareTo).get();
            if (!readStartTime.isAfter(latestModifiedSecond)) {
                remoteFiles = remoteFilesTemp.stream()
                        .filter(e -> e.getFileModifiedTime().isBefore(latestModifiedSecond))
                        .collect(Collectors.toList());
            } else {
                remoteFiles = remoteFilesTemp;
            }
        } catch (Exception exception) {
        }
    }

    @Override
    public void filterFiles() {
        log.info("* TransferBaseHandler filterFiles");
        if (handlerRecord.getConnectionRecord().getTransferFileTimeFilter().equals(FileTimeFilter.MODIFIED)) {
            List<RemoteFileRecord> remoteFilesTemp = remoteFiles.stream()
                    .filter(e -> !e.getFileModifiedTime()
                            .isAfter(handlerRecord.getConnectionRecord().getLastModifiedTime()))
                    .toList();
            remoteFiles.removeAll(remoteFilesTemp);
        }
    }

    @Override
    public void setFileInfo() {
        log.info("* TransferBaseHandler setFileInfo");
    }

    @Override
    public void cacheResults() {
        log.info("* TransferBaseHandler cacheResults");
        final int length = Long.toString(remoteFiles.stream().filter(RemoteFileRecord::getFilter).count()).length() + 2;
        AtomicLong fileId = new AtomicLong(0);
        List<TransferConnectionResult> list
                = remoteFiles.stream()
                .filter(RemoteFileRecord::getFilter)
                .sorted(Comparator.comparing(RemoteFileRecord::getFileName))
                .map(e -> TransferConnectionResult.getResult(handlerRecord, e, length, fileId.incrementAndGet()))
                .toList();
        log.info("* TransferBaseHandler cacheResults resultSize: {}", list.size());
        transferConnectionResultRepository.saveAll(list);
    }

    @Override
    public void setLastModifiedTime() {
        log.info("* TransferBaseHandler setLastModifiedTime");
        remoteFiles.parallelStream()
                .filter(RemoteFileRecord::getFilter)
                .map(RemoteFileRecord::getFileModifiedTime)
                .max(OffsetDateTime::compareTo)
                .ifPresent(this::setLastModifiedTime);
    }

    @Override
    public void clearRemoteFiles() {
        log.info("* TransferBaseHandler clearRemoteFiles");
        remoteFiles.clear();
    }

    @Override
    public void download() {
        log.info("* TransferBaseHandler download");
        List<TransferConnectionResult> list = getConnectionResults();
        log.info("* TransferBaseHandler download resultSize: {}", list.size());
        list.forEach(each -> {
            RemoteFileTransferRecord result = transferConnectionFactory.downloadFile(
                    (handlerRecord.getConnectionRecord().getRemotePath() + "/"
                            + (each.getPathPostfix() == null ? "" : each.getPathPostfix().trim()) + "/"
                            + each.getRemoteFileName()
                    ).replace("//", "/").trim(),
                    (handlerRecord.getRawPath() + "/" +
                            (each.getFileId() != null ? each.getFileId() + "^^" : "") +
                            (each.getLocalFileName() != null ? each.getLocalFileName() : each.getRemoteFileName())
                    ).replace("//", "/").trim()
            );

            if (result.getIsDownloaded()) {
                each.setIsDownloaded(true);
                each.setFileTransferTime(result.getFileTransferTime());
                each.setFileSize(result.getFileSize());
                each.setFileModifiedTimeStr(result.getFileModifiedTimeStr());
                each.setFileModifiedTime(result.getFileModifiedTime());
            }
            each.setTransferTryCount(each.getTransferTryCount() + 1);
            transferConnectionResultRepository.save(each);
        });
    }

    @Override
    public void postHandler() {
        log.info("* TransferBaseHandler postHandler");
    }

    @Override
    public void closeConnection() {
        log.info("* TransferBaseHandler closeConnection");
        transferConnectionFactory.disconnect();
    }

    public void setLastModifiedTime(OffsetDateTime modifiedTime) {
        Connection connection
                = connectionRepository.findById(handlerRecord.getConnectionRecord().getConnectionId()).get();
        connection.setLastModifiedTime(modifiedTime);
        connectionRepository.save(connection);
    }

    public OffsetDateTime getTimeLimit(String timeUnit, Long timeLimit) {
        return switch (timeUnit.toUpperCase()) {
            case "YEARS" -> OffsetDateTime.now().minusYears(timeLimit);
            case "MONTHS" -> OffsetDateTime.now().minusMonths(timeLimit);
            case "WEEKS" -> OffsetDateTime.now().minusWeeks(timeLimit);
            case "DAYS" -> OffsetDateTime.now().minusDays(timeLimit);
            case "HOURS" -> OffsetDateTime.now().minusHours(timeLimit);
            case "MINUTES" -> OffsetDateTime.now().minusMinutes(timeLimit);
            case "SECONDS" -> OffsetDateTime.now().minusSeconds(timeLimit);
            default -> OffsetDateTime.now();
        };
    }

    public List<TransferConnectionResult> getConnectionResults() {
        log.info("* TransferBaseHandler getConnectionResults");
        return transferConnectionResultRepository.getFileListToTransfer(
                handlerRecord.getConnectionRecord().getConnectionId(),
                handlerRecord.getConnectionRecord().getTransferTryCountMinLimit(),
                handlerRecord.getConnectionRecord().getTransferTryCountMaxLimit(),
                getTimeLimit(handlerRecord.getConnectionRecord().getFragmentTimeMinLimitUnit().getValue(),
                        handlerRecord.getConnectionRecord().getFragmentTimeMinLimit()),
                getTimeLimit(handlerRecord.getConnectionRecord().getFragmentTimeMaxLimitUnit().getValue(),
                        handlerRecord.getConnectionRecord().getFragmentTimeMaxLimit())
        );
    }

}
