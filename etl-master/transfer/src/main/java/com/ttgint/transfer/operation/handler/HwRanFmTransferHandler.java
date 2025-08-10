package com.ttgint.transfer.operation.handler;

import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.transfer.base.TransferBaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class HwRanFmTransferHandler extends TransferBaseHandler {

    private final List<String> items;

    public HwRanFmTransferHandler(ApplicationContext applicationContext, TransferHandlerRecord handlerRecord, List<String> items) {
        super(applicationContext, handlerRecord);
        this.items = items;
    }

    @Override
    public void filterFiles() {
        log.info("* HwRanFmTransferHandler filterFiles and setFileInfos");
        super.filterFiles();
        getRemoteFiles().stream()
                .filter(e -> {
                    try {
                        boolean b = items.contains(getItemName(e.getFileName()));
                        getDate(e.getFileName());
                        return b;
                    } catch (Exception exception) {
                        return false;
                    }
                })
                .forEach(e -> {
                    e.setFilter(true);
                    e.setLocalFileName(e.getFileName());
                    e.setSourceItemName(getItemName(e.getFileName()));
                    e.setFragmentTime(getDate(e.getFileName()));
                });
    }

    public String getItemName(String fileName) {
        return fileName.split("-", 2)[1]
                .substring(0, fileName.split("-", 2)[1].lastIndexOf("-"));
    }

    public OffsetDateTime getDate(String fileName) {
        return OffsetDateTime.parse(
                fileName
                        .split("-")[0]
                        .substring(0, 8) + "0000+03:00",
                DateTimeFormatter.ofPattern("yyyyMMddHHmmXXX"));
    }
}
