package com.ttgint.transfer.operation.handler;

import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.transfer.base.TransferBaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class IgCorePmTransferHandler extends TransferBaseHandler {

    private final List<String> nodes;

    public IgCorePmTransferHandler(ApplicationContext applicationContext,
                                   TransferHandlerRecord handlerRecord,
                                   List<String> nodes) {
        super(applicationContext, handlerRecord);
        this.nodes = nodes;
    }

    @Override
    public void filterFiles() {
        log.info("* IgPmCoreTransferHandler filterFiles");
        super.filterFiles();
        getRemoteFiles().stream()
                .filter(e -> e.getFileName().startsWith("A") && e.getFileName().contains(".xml"))
                .filter(e -> {
                    try {
                        boolean b = nodes.contains(e.getFileName().split("_", 2)[1].split("\\.")[0]);
                        OffsetDateTime.parse(
                                e.getFileName().substring(1, 19),
                                DateTimeFormatter.ofPattern("yyyyMMdd.HHmmZ")
                        );
                        return b;
                    } catch (Exception exception) {
                        return false;
                    }
                })
                .forEach(e -> {
                    e.setFilter(true);
                    e.setSourceNodeName(e.getFileName().split("_", 2)[1].split("\\.")[0]);
                    e.setFragmentTime(
                            OffsetDateTime.parse(
                                    e.getFileName().substring(1, 19),
                                    DateTimeFormatter.ofPattern("yyyyMMdd.HHmmZ")));
                });
    }

}
