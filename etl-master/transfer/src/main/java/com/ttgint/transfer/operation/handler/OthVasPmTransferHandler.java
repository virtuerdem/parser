package com.ttgint.transfer.operation.handler;

import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.transfer.base.TransferBaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class OthVasPmTransferHandler extends TransferBaseHandler {

    private final List<String> items;

    public OthVasPmTransferHandler(ApplicationContext applicationContext,
                                   TransferHandlerRecord handlerRecord,
                                   List<String> items) {
        super(applicationContext, handlerRecord);
        this.items = items;
    }

    @Override
    public void filterFiles() {
        log.info("* OthVasPmTransferHandler filterFiles");
        super.filterFiles();
        getRemoteFiles().stream()
                .filter(e -> e.getFileName().contains("_report") && e.getFileName().contains(".csv"))
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
                    e.setSourceItemName(getItemName(e.getFileName()));
                    e.setFragmentTime(getDate(e.getFileName()));
                });
    }

    public OffsetDateTime getDate(String fileName) {
        return OffsetDateTime.parse(
                fileName.split("_")[0] + " 00:00+03:00",
                DateTimeFormatter.ofPattern("yyyyMMdd HH:mmXXX"));
    }

    public String getItemName(String fileName) {
        return fileName.split("_", 2)[1].split("\\.")[0];
    }
}