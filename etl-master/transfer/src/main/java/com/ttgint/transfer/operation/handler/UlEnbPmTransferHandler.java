package com.ttgint.transfer.operation.handler;

import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.transfer.base.TransferBaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class UlEnbPmTransferHandler extends TransferBaseHandler {

    private final List<String> nodes;

    public UlEnbPmTransferHandler(ApplicationContext applicationContext,
                                  TransferHandlerRecord handlerRecord,
                                  List<String> nodes) {
        super(applicationContext, handlerRecord);
        this.nodes = nodes;
    }

    @Override
    public void filterFiles() {
        log.info("* UlEnbPmTransferHandler filterFiles and setFileInfos");
        super.filterFiles();
        getRemoteFiles().stream()
                .filter(e -> {
                    try {
                        // Dosya adından node ID'sini çıkar (49501)
                        String nodeId = getNodeName(e.getFileName());
                        System.out.println("Dosya: " + e.getFileName() + ", Node ID: " + nodeId);

                        // Veritabanındaki node_code'ları kontrol et
                        boolean found = false;
                        for (String node : nodes) {
                            if (node.contains(nodeId)) {
                                found = true;
                                break;
                            }
                        }

                        getDate(e.getFileName());
                        return found;
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
        // 1..<19) arası = "20250413.2300+0300"
        return OffsetDateTime.parse(
                fileName.substring(1, 19),
                DateTimeFormatter.ofPattern("yyyyMMdd.HHmmZ")
        );
    }


    public String getNodeName(String fileName) {
        return fileName.substring(fileName.lastIndexOf('_') + 1, fileName.lastIndexOf('.'));
    }
}