package com.ttgint.transfer.operation.handler;

import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.transfer.base.TransferBaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class HwRanInfoReportCmTransferHandler extends TransferBaseHandler {

    private final List<String> items;

    public HwRanInfoReportCmTransferHandler(ApplicationContext applicationContext,
                                            TransferHandlerRecord handlerRecord,
                                            List<String> items) {
        super(applicationContext, handlerRecord);
        this.items = items;
    }

    @Override
    public void filterFiles() {
        log.info("* HwRanInfoReportCmTransferHandler filterFiles and setFileInfos");
        super.filterFiles();
        getRemoteFiles().stream()
                .filter(e -> e.getFileName().contains(".csv"))
                .filter(e -> {
                    try {
                        getDate(e.getFileName());
                        return items.contains(getItemName(e.getFileName()));
                    } catch (Exception exception) {
                        exception.printStackTrace();
                        return false;
                    }
                })
                .forEach(e -> {
                    e.setFilter(true);
                    e.setFragmentTime(getDate(e.getFileName()));
                    e.setSourceItemName(getItemName(e.getFileName()));
                });
    }

    public OffsetDateTime getDate(String fileName) {
        return OffsetDateTime.parse(
                fileName.split("_")[fileName.split("_").length - 1]
                        .substring(0, 8) + " 00:00+03:00",
                DateTimeFormatter.ofPattern("yyyyMMdd HH:mmXXX"));
    }

    public String getItemName(String fileName) {
        return fileName.replace("_" + fileName.split("_")[fileName.split("_").length - 1], "");
    }
}
