package com.ttgint.transfer.operation.handler;

import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.transfer.base.TransferBaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class HwNbPmTransferHandler extends TransferBaseHandler {

    private final List<String> nodes;

    public HwNbPmTransferHandler(ApplicationContext applicationContext,
                                 TransferHandlerRecord handlerRecord,
                                 List<String> nodes) {
        super(applicationContext, handlerRecord);
        this.nodes = nodes;
    }

    @Override
    public void filterFiles() {
        log.info("* HwNbPmTransferHandler filterFiles and setFileInfos");
        super.filterFiles();
        getRemoteFiles().stream()
                .filter(e -> e.getFileName().startsWith("A") && e.getFileName().contains(".xml")
                        && e.getFileName().split("_")[1].startsWith("R")
                        && e.getFileName().split("\\.")[2].startsWith("N"))
                .filter(e -> {
                    try {
                        boolean b = nodes.contains(e.getFileName().split("\\.")[2]);
                        getDate(e.getFileName());
                        return b;
                    } catch (Exception exception) {
                        return false;
                    }
                })
                .forEach(e -> {
                    e.setFilter(true);
                    e.setSourceNodeName(e.getFileName().split("\\.")[2]);
                    e.setFragmentTime(getDate(e.getFileName()));
                });
    }

    public OffsetDateTime getDate(String fileName) {
        return OffsetDateTime.parse(
                fileName.split("A")[1].substring(0, 18),
                DateTimeFormatter.ofPattern("yyyyMMdd.HHmmZ")
        );
    }
}
