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
public class OthTwampCmTransferHandler extends TransferBaseHandler {

    private final List<String> items;

    public OthTwampCmTransferHandler(ApplicationContext applicationContext,
                                     TransferHandlerRecord handlerRecord,
                                     List<String> items) {
        super(applicationContext, handlerRecord);
        this.items = items;
    }

    @Override
    public void filterFiles() {
        log.info("* OthTwampCmTransferHandler filterFiles and setFileInfos");
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
                    e.setSourceItemName(getItemName(e.getFileName()));
                    e.setFragmentTime(getDate(e.getFileName()));
                });
    }

    public OffsetDateTime getDate(String fileName) {
        String[] split = fileName.split("-");
        return OffsetDateTime.parse(
                split[split.length - 3] +
                        split[split.length - 2] +
                        split[split.length - 1].split("\\.")[0] + " 0000+03:00",
                DateTimeFormatter.ofPattern("yyyyMMdd HHmmXXX")
        );
    }

    public String getItemName(String fileName) {
        return fileName.split("-")[0];
    }
}
