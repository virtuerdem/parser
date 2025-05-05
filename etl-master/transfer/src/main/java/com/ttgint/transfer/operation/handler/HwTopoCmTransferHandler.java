package com.ttgint.transfer.operation.handler;

import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.transfer.base.TransferBaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class HwTopoCmTransferHandler extends TransferBaseHandler {

    private final List<String> items;

    public HwTopoCmTransferHandler(ApplicationContext applicationContext,
                                   TransferHandlerRecord handlerRecord,
                                   List<String> items) {
        super(applicationContext, handlerRecord);
        this.items = items;
    }

    @Override
    public void filterFiles() {
        log.info("* HwTopoCmTransferHandler filterFiles and setFileInfos");
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
                    e.setSourceItemName(getItemName(e.getFileName()));
                    e.setFragmentTime(getDate(e.getFileName()));
                });
    }

    public OffsetDateTime getDate(String fileName) {
        return OffsetDateTime.parse(
                fileName
                        .split("_")[fileName.split("_").length - 3] + " 00:00+03:00",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mmXXX")
        );
    }

    public String getItemName(String fileName) {
        String[] split = fileName.split("_");
        return fileName.replace("_" +
                split[split.length - 3] + "_" +
                split[split.length - 2] + "_" +
                split[split.length - 1], "");
    }
}