package com.ttgint.library.record;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class RemoteFileTransferRecord {

    private Long fileSize;
    private OffsetDateTime fileModifiedTime;
    private Long fileModifiedTimeStr;
    private Boolean isDownloaded;
    private OffsetDateTime fileTransferTime;

    public RemoteFileTransferRecord(Boolean isDownloaded) {
        this.isDownloaded = isDownloaded;
    }

}
