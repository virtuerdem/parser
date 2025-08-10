package com.ttgint.transfer.operation.handler;

import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.transfer.base.TransferBaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class HwNbCsvPmTransferHandler extends TransferBaseHandler {

    private final List<String> items;

    public HwNbCsvPmTransferHandler(ApplicationContext applicationContext,
                                    TransferHandlerRecord handlerRecord,
                                    List<String> items) {
        super(applicationContext, handlerRecord);
        this.items = items;
    }

    @Override
    public void filterFiles() {
        log.info("* HwNbCsvPmTransferHandler filterFiles and setFileInfos");
        super.filterFiles();
        getRemoteFiles().stream()
                .filter(e -> e.getFileName().contains("_pmresult_") && e.getFileName().contains(".csv"))
                .filter(e -> {
                    try {
                        boolean b
                                = items.contains("pmresult_"
                                + e.getFileName().split("_", 6)[2] + "_" // itemCode
                                + e.getFileName().split("_", 6)[3] + "_" // timePeriod
                        );
                        getDate(e.getFileName());
                        return b;
                    } catch (Exception exception) {
                        return false;
                    }
                })
                .forEach(e -> {
                    e.setFilter(true);
                    e.setSourceItemName(e.getFileName().split("_")[2]);
                    e.setFragmentTime(getDate(e.getFileName()));
                });
    }

    public OffsetDateTime getDate(String fileName) {
        return OffsetDateTime.parse(
                fileName
                        .split("_")[fileName.split("_").length - 2] + " +03:00",
                DateTimeFormatter.ofPattern("yyyyMMddHHmm XXX")
        );
    }
}
