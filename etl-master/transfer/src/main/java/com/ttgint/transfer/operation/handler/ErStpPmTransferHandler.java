package com.ttgint.transfer.operation.handler;

import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.transfer.base.TransferBaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public class ErStpPmTransferHandler extends TransferBaseHandler {

    public ErStpPmTransferHandler(ApplicationContext applicationContext, TransferHandlerRecord handlerRecord) {
        super(applicationContext, handlerRecord);
    }

    @Override
    public void filterFiles() {
        log.info("* ErStpPmTransferHandler filterFiles");
        super.filterFiles();
        getRemoteFiles().stream()
                .filter(e -> e.getFileName().startsWith("C") && e.getFileName().contains(".asn1")
                        && e.getFileName().contains("=STP"))
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
                    e.setSourceNodeName(e.getFileName().split("=")[1].split("_")[0]);
                    e.setFragmentTime(OffsetDateTime.parse(e.getFileName().substring(1, 19),
                            DateTimeFormatter.ofPattern("yyyyMMdd.HHmmZ")));
                });
    }
}
