package com.ttgint.transfer.operation.handler;

import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.transfer.base.TransferBaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class HwMwPmTransferHandler extends TransferBaseHandler {

    private final List<String> items;

    public HwMwPmTransferHandler(ApplicationContext applicationContext,
                                 TransferHandlerRecord handlerRecord,
                                 List<String> items) {
        super(applicationContext, handlerRecord);
        this.items = items;
    }

    @Override
    public void filterFiles() {
        log.info("* HwMwPmTransferHandler filterFiles and setFileInfos");
        super.filterFiles();
        getRemoteFiles().stream()
                .filter(e -> e.getFileName().contains(".csv"))
                .filter(e -> {
                    try {
                        getDate(e.getFileName());
                        return items.contains(getItemName(e.getFileName()));
                    } catch (Exception exception) {
                        return false;
                    }
                })
                .forEach(e -> {
                    e.setFilter(true);
                    e.setFragmentTime(getDate(e.getFileName()));
                    e.setSourceItemName(getItemName(e.getAbsolutePath()));
                });
    }

    public OffsetDateTime getDate(String fileName) {
        String fileDate = fileName.split("_")[fileName.split("_").length - 2] + " 00:00+03:00";
        return OffsetDateTime.parse(
                fileDate,
                DateTimeFormatter.ofPattern(
                        (fileDate.split("-")[0].length() == 4 ? "yyyy-MM-dd" : "MM-dd-yyyy") + " HH:mmXXX")
        );
    }

    public String getItemName(String fileName) {
        String[] split = fileName.split("_");
        return fileName.replace("_" + split[split.length - 2] + "_" + split[split.length - 1], "");
    }
}
