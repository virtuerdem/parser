package com.ttgint.transfer.operation.handler;

import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.transfer.base.TransferBaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class OthHpeKpiCmTransferHandler extends TransferBaseHandler {

    private final List<String> items;
    private final List<String> nodes;

    public OthHpeKpiCmTransferHandler(ApplicationContext applicationContext,
                                  TransferHandlerRecord handlerRecord,
                                  List<String> items,
                                  List<String> nodes) {
        super(applicationContext, handlerRecord);
        this.items = items;
        this.nodes = nodes;
    }


    @Override
    public void filterFiles() {
        log.info("* OthHpeKpiCmTransferHandler filterFiles and setFileInfos");
        super.filterFiles();
        getRemoteFiles().stream()
                .filter(e ->
                        e.getFileName().startsWith("degapp")
                                && e.getFileName().endsWith(".gz")
                )
                .filter(e -> {
                    try {
                        boolean b = items.contains(e.getFileName().split("_", 3)[2].split("\\.")[0])
                                && nodes.contains(e.getFileName().split("_")[0]);
                        OffsetDateTime.parse(
                                getDate(e.getFileName()) + " +03:00",
                                DateTimeFormatter.ofPattern("yyyyMMddHHmm XXX")
                        );
                        return b;
                    } catch (Exception exception) {
                        return false;
                    }
                })
                .forEach(e -> {
                    e.setFilter(true);
                    e.setSourceItemName(e.getFileName().split("_", 3)[2].split("\\.")[0]);
                    e.setSourceNodeName(e.getFileName().split("_")[0]);
                    e.setFragmentTime(
                            OffsetDateTime.parse(
                                    getDate(e.getFileName()) + " +03:00",
                                    DateTimeFormatter.ofPattern("yyyyMMddHHmm XXX")
                            )
                    );
                });
    }

    public String getDate(String fileName) {
        return fileName.split("_", 3)[1].substring(0, fileName.split("_", 3)[1].length() - 2);
    }
}
