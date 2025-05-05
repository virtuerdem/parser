package com.ttgint.transfer.operation.handler;

import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.transfer.base.TransferBaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class HwNbCmTransferHandler extends TransferBaseHandler {

    private final List<String> nodes;

    public HwNbCmTransferHandler(ApplicationContext applicationContext,
                                 TransferHandlerRecord handlerRecord,
                                 List<String> nodes) {
        super(applicationContext, handlerRecord);
        this.nodes = nodes;
    }

    @Override
    public void filterFiles() {
        log.info("* HwNbCmTransferHandler filterFiles and setFileInfos");
        super.filterFiles();
        getRemoteFiles().stream()
                .filter(e -> e.getFileName().startsWith("CMExport_N")
                        && !e.getFileName().startsWith("CMExport_NR"))
                .filter(e -> {
                    try {
                        boolean b = nodes.contains(getNodeName(e.getFileName()));
                        getDate(e.getFileName());
                        return b;
                    } catch (Exception exception) {
                        return false;
                    }
                })
                .collect(Collectors.toMap(
                        e -> getNodeName(e.getFileName()),
                        e -> e,
                        (node1, node2) -> {
                            OffsetDateTime date1 = node1.getFileModifiedTime();
                            OffsetDateTime date2 = node2.getFileModifiedTime();
                            return date1.isAfter(date2) ? node1 : node2;
                        }))
                .values()
                .forEach(e -> {
                    e.setFilter(true);
                    e.setSourceNodeName(getNodeName(e.getFileName()));
                    e.setFragmentTime(getDate(e.getFileName()));
                });
    }

    public OffsetDateTime getDate(String fileName) {
        return OffsetDateTime.parse(
                fileName
                        .split("_")[fileName.split("_").length - 1]
                        .substring(0, 8) + "00+03:00",
                DateTimeFormatter.ofPattern("yyyyMMddHHXXX")
        );
    }

    public String getNodeName(String fileName) {
        return fileName
                .replace(fileName.split("_")[0] + "_", "")
                .replace("_" + fileName.split("_")[fileName.split("_").length - 2], "")
                .replace("_" + fileName.split("_")[fileName.split("_").length - 1], "");
    }

}
