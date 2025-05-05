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
public class NkCsPmTransferHandler extends TransferBaseHandler {

    private final List<String> items;

    public NkCsPmTransferHandler(ApplicationContext applicationContext,
                                 TransferHandlerRecord handlerRecord,
                                 List<String> items) {
        super(applicationContext, handlerRecord);
        this.items = items;
    }

    @Override
    public void filterFiles() {
        log.info("* NkCsPmTransferHandler filterFiles and setFileInfos");
        super.filterFiles();
        getRemoteFiles().stream()
                .filter(e -> e.getFileName().startsWith("PM") && e.getFileName().contains(".xml"))
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
        return OffsetDateTime.parse(
                fileName.split("PM")[1].substring(0, 17),
                DateTimeFormatter.ofPattern("yyyyMMddHHmmZ")
        ).truncatedTo(ChronoUnit.HOURS);
    }

    public String getItemName(String fileName) {
        return fileName.substring(21).split("\\.")[0].split("\\_")[0];
    }
}
