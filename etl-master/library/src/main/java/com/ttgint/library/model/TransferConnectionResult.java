package com.ttgint.library.model;

import com.ttgint.library.record.RemoteFileRecord;
import com.ttgint.library.record.TransferHandlerRecord;
import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;

@Table(name = "t_transfer_connection_result")
@Entity
@Data
@Builder
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class TransferConnectionResult implements Serializable {

    @Id
    @SequenceGenerator(name = "t_transfer_connection_result_seq_id", sequenceName = "t_transfer_connection_result_seq_id", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "t_transfer_connection_result_seq_id")
    private Long id;
    private Long flowId;
    private String flowProcessCode;
    private Long connectionId;
    private String connectionHistoryCode;
    private String pathPostfix;
    private String remoteFileName;
    private String fileId;
    private String localFileName;
    private OffsetDateTime fragmentTime;
    private Boolean isDownloaded;
    private Integer transferTryCount;
    private Long fileSize;
    private String sourceNodeName;
    private String sourceElementName;
    private String sourceItemName;
    private OffsetDateTime fileModifiedTime;
    private Long fileModifiedTimeStr;
    private Boolean isRead;
    private OffsetDateTime fileReadTime;
    private OffsetDateTime fileTransferTime;

    private OffsetDateTime createdTime;

    public static TransferConnectionResult getResult(TransferHandlerRecord handlerRecord,
                                                     RemoteFileRecord record,
                                                     int lengthOfFileId,
                                                     long fileId) {
        TransferConnectionResult result = new TransferConnectionResult();
        result.setFlowId(handlerRecord.getFlowId());
        result.setFlowProcessCode(handlerRecord.getFlowProcessCode());
        result.setConnectionId(record.getConnectionId());
        result.setConnectionHistoryCode(handlerRecord.getConnectionRecord().getConnectionHistoryCode());

        result.setPathPostfix(record.getFilePath().replace(handlerRecord.getConnectionRecord().getRemotePath(), ""));
        if (result.getPathPostfix().isEmpty() || result.getPathPostfix().isBlank()) {
            result.setPathPostfix(null);
        }

        result.setRemoteFileName(record.getFileName());
        result.setFileId(handlerRecord.getConnectionRecord().getConnectionHistoryCode() +
                String.format("%1$" + lengthOfFileId + "s", fileId).replace(' ', '0'));
        result.setLocalFileName(record.getLocalFileName());
        result.setFragmentTime(record.getFragmentTime() == null
                ? OffsetDateTime.now().truncatedTo(ChronoUnit.HOURS)
                : record.getFragmentTime());
        result.setIsDownloaded(false);
        result.setTransferTryCount(0);
        result.setFileSize(record.getFileSize());
        result.setSourceNodeName(record.getSourceNodeName());
        result.setSourceElementName(record.getSourceElementName());
        result.setSourceItemName(record.getSourceItemName());
        result.setFileModifiedTime(record.getFileModifiedTime());
        result.setFileModifiedTimeStr(record.getFileModifiedTimeStr());
        result.setIsRead(true);
        result.setFileReadTime(record.getFileReadTime());
        result.setFileTransferTime(null);
        result.setCreatedTime(OffsetDateTime.now());
        return result;
    }
}