package com.ttgint.transfer.operation.handler;

import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.transfer.base.TransferBaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
public class HwRanInfoTrx2gCmTransferHandler extends TransferBaseHandler {

    private final List<String> items;

    public HwRanInfoTrx2gCmTransferHandler(ApplicationContext applicationContext,
                                           TransferHandlerRecord handlerRecord,
                                           List<String> items) {
        super(applicationContext, handlerRecord);
        this.items = items;
    }

    @Override
    public void filterFiles() {
        log.info("* HwRanInfoTrx2gCmTransferHandler filterFiles and setFileInfos");
        super.filterFiles();
        getRemoteFiles().stream()
                .filter(e -> e.getFileName().contains(".xls") && !e.getFileName().contains(".bak"))
                .filter(e -> {
                    try {
                        return items.contains(getItemName(e.getFileName()));
                    } catch (Exception exception) {
                        return false;
                    }
                })
                .forEach(e -> {
                    e.setFilter(true);
                    e.setFragmentTime(getDate(e.getFileModifiedTime()));
                    e.setSourceItemName(getItemName(e.getFileName()));
                    e.setLocalFileName(getLocalFileName(e.getFileName(), e.getFileModifiedTime()));
                });
    }

    public OffsetDateTime getDate(OffsetDateTime fileModifiedTime) {
        return fileModifiedTime.truncatedTo(ChronoUnit.DAYS);
    }

    public String getItemName(String fileName) {
        return fileName.split("\\.")[0];
    }

    public String getLocalFileName(String fileName, OffsetDateTime fileModifiedTime) {
        return fileName.replace(".xls",
                "-" + getDate(fileModifiedTime).format(DateTimeFormatter.ofPattern("yyyyMMddHHmmZ")) + ".xls");
    }
}
