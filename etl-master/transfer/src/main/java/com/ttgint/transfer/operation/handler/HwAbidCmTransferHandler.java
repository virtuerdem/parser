package com.ttgint.transfer.operation.handler;

import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.transfer.base.TransferBaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class HwAbidCmTransferHandler extends TransferBaseHandler {

    private final List<String> items;

    public HwAbidCmTransferHandler(ApplicationContext applicationContext,
                                   TransferHandlerRecord handlerRecord,
                                   List<String> items) {
        super(applicationContext, handlerRecord);
        this.items = items;
    }

    @Override
    public void filterFiles() {
        log.info("* HwAbidCmTransferHandler filterFiles and setFileInfos");
        super.filterFiles();
        getRemoteFiles().stream()
                .filter(e -> e.getFileName().startsWith("MSC")
                        && e.getFileName().contains("abid")
                        && e.getFileName().contains(".txt"))
                .filter(e -> {
                    try {
                        boolean b = items.contains(e.getFileName().split("-")[0]);
                        getDate(e.getFileName());
                        return b;
                    } catch (Exception exception) {
                        return false;
                    }
                })
                .forEach(e -> {
                    e.setFilter(true);
                    e.setSourceItemName(e.getFileName().split("-")[0]);
                    e.setFragmentTime(getDate(e.getFileName()));
                });
    }

    public OffsetDateTime getDate(String fileName) {
        return OffsetDateTime.parse(
                fileName
                        .split("-", 2)[1]
                        .substring(0, 10) + "00+03:00",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mmXXX")
        );
    }

}
