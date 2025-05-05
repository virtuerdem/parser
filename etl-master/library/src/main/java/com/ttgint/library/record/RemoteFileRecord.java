package com.ttgint.library.record;

import com.ttgint.library.enums.FileInfo;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class RemoteFileRecord {

    private Long connectionId;
    private String filePath;
    private String fileName;
    private String absolutePath;
    @Enumerated(EnumType.STRING)
    private FileInfo fileInfo;
    private Long fileSize;
    private OffsetDateTime fileModifiedTime;
    private Long fileModifiedTimeStr;
    private OffsetDateTime fileReadTime;

    //setFileInfo for TransferResult
    private Boolean filter;
    private String localFileName;
    private OffsetDateTime fragmentTime;
    private String sourceNodeName;
    private String sourceElementName;
    private String sourceItemName;

    public static RemoteFileRecord getRecord(Long connectionId,
                                             String filePath,
                                             String fileName,
                                             FileInfo fileInfo,
                                             Long fileSize,
                                             OffsetDateTime fileModifiedTime,
                                             Long fileModifiedTimeStr) {
        RemoteFileRecord record = new RemoteFileRecord();
        record.setConnectionId(connectionId);
        record.setFilePath(filePath);
        record.setFileName(fileName);
        record.setAbsolutePath((filePath + "/" + fileName).replace("//", "/"));
        record.setFileInfo(fileInfo);
        record.setFileSize(fileSize);
        record.setFileModifiedTime(fileModifiedTime);
        record.setFileModifiedTimeStr(fileModifiedTimeStr);
        record.setFileReadTime(OffsetDateTime.now());
        record.setFilter(false);
        return record;
    }

}
