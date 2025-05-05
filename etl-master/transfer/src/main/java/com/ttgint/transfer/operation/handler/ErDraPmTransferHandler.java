package com.ttgint.transfer.operation.handler;

import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.transfer.base.TransferBaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class ErDraPmTransferHandler extends TransferBaseHandler {

    public ErDraPmTransferHandler(ApplicationContext applicationContext, TransferHandlerRecord handlerRecord) {
        super(applicationContext, handlerRecord);
    }

    @Override
    public void filterFiles() {
        log.info("* DefaultTransferHandler filterFiles");
        super.filterFiles();
        getRemoteFiles().stream()
                .filter(e -> e.getFileName().startsWith("A") && e.getFileName().contains(".xml")
                        && e.getFileName().contains("=dra"))
                .filter(e -> {
                    try {
                        OffsetDateTime.parse(
                                e.getFileName().substring(1, 19),
                                DateTimeFormatter.ofPattern("yyyyMMdd.HHmmZ")
                        );
                        return true;
                    } catch (Exception exception) {
                        return false;
                    }
                })
                .forEach(e -> {
                    e.setFilter(true);
                    e.setSourceNodeName("dra" + e.getFileName().split("=dra")[1].split("\\,")[0]);
                    e.setFragmentTime(OffsetDateTime.parse(e.getFileName().substring(1, 19),
                            DateTimeFormatter.ofPattern("yyyyMMdd.HHmmZ")));
                });

    }
}
