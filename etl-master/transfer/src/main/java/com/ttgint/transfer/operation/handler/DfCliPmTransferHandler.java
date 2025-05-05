package com.ttgint.transfer.operation.handler;

import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.transfer.base.TransferBaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class DfCliPmTransferHandler extends TransferBaseHandler {
    private final List<String> items;

    public DfCliPmTransferHandler(ApplicationContext applicationContext,
                                  TransferHandlerRecord handlerRecord,
                                  List<String> items) {
        super(applicationContext, handlerRecord);
        this.items = items;
    }

    @Override
    public void filterFiles() {
        log.info("* DfCliTransferHandler filterFiles and setFileInfos");
        super.filterFiles();
        getRemoteFiles().stream()
                .filter(e -> e.getFileName().contains("kpi.csv.")
                        && !e.getFileName().contains(".gz")
                        && !e.getFileName().contains(".log"))
                .filter(e -> {
                    try {
                        boolean b = items.contains(getItemName(e.getAbsolutePath()));
                        OffsetDateTime.parse(
                                e.getFileName()
                                        .split(".csv.")[1]
                                        .substring(0, 13) + ":00+03:00",
                                DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mmXXX")
                        );
                        return b;
                    } catch (Exception exception) {
                        return false;
                    }
                })
                .forEach(e -> {
                    e.setFilter(true);
                    e.setLocalFileName(getLocalFileName(e.getAbsolutePath()));
                    e.setSourceItemName(getItemName(e.getAbsolutePath()));
                    e.setFragmentTime(getDate(e.getFileName()));
                });
    }

    public String getLocalFileNamePrefix(String absolutePath) {
        return absolutePath
                .split("/", 4)[3] // todo : delete that split on prod
                .replace("/", "_");
    }

    public String getLocalFileName(String absolutePath) {
        return getHandlerRecord().getConnectionRecord().getIp() + "+"
                + getLocalFileNamePrefix(absolutePath)
                + ".csv";
    }

    public String getItemName(String absolutePath) {
        return getLocalFileNamePrefix(absolutePath)
                .split("kpi.csv.")[0]
                .replaceAll(".$", "");
    }

    public OffsetDateTime getDate(String fileName) {
        OffsetDateTime date = OffsetDateTime.parse(
                fileName
                        .split(".csv.")[1]
                        .substring(0, 13) + ":00+03:00",
                DateTimeFormatter.ofPattern("yyyy-MM-dd-HH:mmXXX"));
        if (fileName.endsWith("-12-1.csv")) {
            date = date.minusHours(12);
        } else if (!fileName.endsWith("-12-2.csv") && fileName.endsWith("-2.csv")) {
            date = date.plusHours(12);
        }
        return date;
    }
}
