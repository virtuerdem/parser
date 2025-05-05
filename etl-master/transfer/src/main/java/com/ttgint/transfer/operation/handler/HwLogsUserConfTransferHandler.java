package com.ttgint.transfer.operation.handler;

import com.ttgint.library.record.TransferHandlerRecord;
import com.ttgint.transfer.base.TransferBaseHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
public class HwLogsUserConfTransferHandler extends TransferBaseHandler {

    private final List<String> items;

    private final String pathFilter = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "/userlogs/";

    public HwLogsUserConfTransferHandler(ApplicationContext applicationContext,
                                         TransferHandlerRecord handlerRecord,
                                         List<String> items) {
        super(applicationContext, handlerRecord);
        this.items = items;
    }

    @Override
    public void filterFiles() {
        log.info("* HwLogsUserConfTransferHandler filterFiles and setFileInfos");
        super.filterFiles();
        getRemoteFiles().stream()
                .filter(e -> e.getFileName().contains(".csv") && checkFilePath(e.getFilePath()))
                .filter(e -> {
                    try {
                        getDate(e.getFileName());
                        return items.contains(getItemName(e.getFileName()));
                    } catch (Exception exception) {
                        return false;
                    }
                })
                .forEach(e -> {
                    e.setFilter(true);
                    e.setFragmentTime(getDate(e.getFileName()));
                    e.setSourceItemName(getItemName(e.getAbsolutePath()));
                });
    }

    public Boolean checkFilePath(String filePath) {
        return ("/" + filePath + "/").replace("//", "/")
                .equals((getHandlerRecord().getConnectionRecord().getRemotePath() + "/" + pathFilter)
                        .replace("//", "/")
                );
    }

    public OffsetDateTime getDate(String fileName) {
        return OffsetDateTime.parse(
                fileName
                        .split("-")[0]
                        .substring(0, 8) + " 00:00+03:00",
                DateTimeFormatter.ofPattern("yyyyMMdd HH:mmXXX"));
    }

    public String getItemName(String fileName) {
        String[] split = fileName.split("-");
        return fileName
                .replace(split[0] + "-", "")
                .replace("-" + split[split.length - 1], "");
    }
}
