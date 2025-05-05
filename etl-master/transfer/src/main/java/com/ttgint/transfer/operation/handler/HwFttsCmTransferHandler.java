package com.ttgint.transfer.operation.handler;

import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.transfer.base.TransferBaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class HwFttsCmTransferHandler extends TransferBaseHandler {
    private final List<String> items;

    public HwFttsCmTransferHandler(ApplicationContext applicationContext,
                                   TransferHandlerRecord handlerRecord,
                                   List<String> items) {
        super(applicationContext, handlerRecord);
        this.items = items;
    }

    @Override
    public void filterFiles() {
        log.info("* HwFttsCmTransferHandler filterFiles and setFileInfos");
        super.filterFiles();
        getRemoteFiles().stream()
                .filter(e -> e.getFileName().startsWith("FTTS") && e.getFileName().contains(".csv"))
                .filter(e -> {
                    try {
                        boolean b = items.contains(e.getFileName().split("_")[0]);
                        OffsetDateTime.parse(
                                e.getFileName().split("_")[1].substring(0, 8) + " 00:00+03:00",
                                DateTimeFormatter.ofPattern("yyyyMMdd HH:mmXXX"));
                        return b;
                    } catch (Exception exception) {
                        return false;
                    }
                })
                .forEach(e -> {
                    e.setFilter(true);
                    e.setSourceItemName(e.getFileName().split("_")[0]);
                    e.setFragmentTime(
                            OffsetDateTime.parse(
                                    e.getFileName().split("_")[1].substring(0, 8) + " 00:00+03:00",
                                    DateTimeFormatter.ofPattern("yyyyMMdd HH:mmXXX")
                            )
                    );
                });
    }
}
