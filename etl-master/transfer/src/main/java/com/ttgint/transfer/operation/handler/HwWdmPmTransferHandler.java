package com.ttgint.transfer.operation.handler;

import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.transfer.base.TransferBaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class HwWdmPmTransferHandler extends TransferBaseHandler {

    public HwWdmPmTransferHandler(ApplicationContext applicationContext,
                                  TransferHandlerRecord handlerRecord) {
        super(applicationContext, handlerRecord);
    }

    @Override
    public void filterFiles() {
        log.info("* HwWdmPmTransferHandler filterFiles and setFileInfos");
        super.filterFiles();

        getRemoteFiles().stream()
                .filter(e -> {
                    try {
                        boolean isValid = e.getFileName().endsWith(".csv") && e.getFileName().startsWith("PM_");

                        if (isValid) {
                            String dateStr = getDate(e.getFileName());
                            if (dateStr == null) {
                                log.error("Date parsing failed for file: {}", e.getFileName());
                                return false;
                            }
                            OffsetDateTime.parse(dateStr + " +03:00",
                                    DateTimeFormatter.ofPattern("yyyyMMddHHmm XXX"));
                        }
                        return isValid;
                    } catch (Exception exception) {
                        log.error("Error processing file: {}", e.getFileName(), exception);
                        return false;
                    }
                })
                .forEach(e -> {
                    e.setFilter(true);
                    e.setSourceItemName(e.getFileName());

                    String dateStr = getDate(e.getFileName());
                    if (dateStr != null) {
                        e.setFragmentTime(
                                OffsetDateTime.parse(dateStr + " +03:00",
                                        DateTimeFormatter.ofPattern("yyyyMMddHHmm XXX"))
                        );
                    }
                });
    }

    private String getDate(String fileName) {
        try {
            if (fileName.startsWith("PM_")) {
                return fileName.split("_")[3].replace("Z", "");
            }
        } catch (Exception e) {
            log.error("Failed to parse date for file: {}", fileName, e);
        }
        return null;
    }


}