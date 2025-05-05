package com.ttgint.transfer.operation.handler;

import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.transfer.base.TransferBaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class HwRncPmTransferHandler extends TransferBaseHandler {

    private final List<String> nodes;

    public HwRncPmTransferHandler(ApplicationContext applicationContext,
                                  TransferHandlerRecord handlerRecord,
                                  List<String> nodes) {
        super(applicationContext, handlerRecord);
        this.nodes = nodes;
    }

    @Override
    public void filterFiles() {
        log.info("* HwRncPmTransferHandler filterFiles and setFileInfos");
        super.filterFiles();
        getRemoteFiles().stream()
                .filter(e -> e.getFileName().startsWith("A") && e.getFileName().contains(".xml")
                        && e.getFileName().split("_")[1].startsWith("R"))
                .filter(e -> {
                    try {
                        boolean b = nodes.contains(getNodeName(e.getFileName()));
                        getDate(e.getFileName());
                        return b;
                    } catch (Exception exception) {
                        return false;
                    }
                })
                .forEach(e -> {
                    e.setFilter(true);
                    e.setSourceNodeName(getNodeName(e.getFileName()));
                    e.setFragmentTime(getDate(e.getFileName()));
                });
    }

    public OffsetDateTime getDate(String fileName) {
        return OffsetDateTime.parse(
                fileName.split("A")[1].substring(0, 18),
                DateTimeFormatter.ofPattern("yyyyMMdd.HHmmZ")
        );
    }

    public String getNodeName(String fileName) {
        if (fileName.split("_")[fileName.split("_").length - 1].startsWith("P0")) {
            return fileName
                    .split("_", 2)[1]
                    .split("\\.")[0]
                    .replace("_" + fileName.split("_")[fileName.split("_").length - 1], "");
        } else {
            return fileName.split("_", 2)[1].split("\\.")[0];
        }
    }
}