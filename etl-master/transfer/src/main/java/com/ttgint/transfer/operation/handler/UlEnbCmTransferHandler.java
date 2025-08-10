package com.ttgint.transfer.operation.handler;

import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.transfer.base.TransferBaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class UlEnbCmTransferHandler extends TransferBaseHandler {

    public UlEnbCmTransferHandler(ApplicationContext applicationContext, TransferHandlerRecord handlerRecord) {
        super(applicationContext, handlerRecord);
    }

    @Override
    public void filterFiles() {
        log.info("* UlEnbCmTransferHandler filterFiles");
        super.filterFiles();
        getRemoteFiles().stream()
                .filter(e -> e.getFileName().startsWith("CMExport") && e.getFileName().contains(".xml"))
                .filter(e -> {
                    try {
                        getDate(e.getFileName());
                        return true;
                    } catch (Exception exception) {
                        return false;
                    }
                })
                .forEach(e -> {
                    e.setFilter(true);
                    e.setSourceItemName(e.getFileName().split("_")[0]);
                    e.setFragmentTime(getDate(e.getFileName()));
                });
    }

    public OffsetDateTime getDate(String fileName) {
        return OffsetDateTime.parse(
                fileName
                        .split("_")[1]
                        .substring(0, 10) + " 00:00+03:00",
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mmXXX")
        );
    }

}
