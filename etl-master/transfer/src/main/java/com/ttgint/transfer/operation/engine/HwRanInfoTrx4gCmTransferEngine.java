package com.ttgint.transfer.operation.engine;

import com.ttgint.library.model.NetworkItem;
import com.ttgint.library.record.DecompressRecord;
import com.ttgint.library.repository.NetworkItemRepository;
import com.ttgint.transfer.base.TransferBaseEngine;
import com.ttgint.transfer.operation.handler.HwRanInfoTrx4gCmTransferHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Component("HW_RI_TRX4G_CM_TRANSFER")
public class HwRanInfoTrx4gCmTransferEngine extends TransferBaseEngine {

    private final NetworkItemRepository networkItemRepository;

    private List<NetworkItem> networkItems;

    public HwRanInfoTrx4gCmTransferEngine(ApplicationContext applicationContext) {
        super(applicationContext);
        this.networkItemRepository = applicationContext.getBean(NetworkItemRepository.class);
    }

    @Override
    protected void preEngine() {
        log.info("* HwRanInfoTrx4gCmTransferEngine preEngine");
        this.networkItems = networkItemRepository.findByFlowIdAndIsActive(engineRecord.getFlowId(), true);
    }

    @Override
    protected void onEngine() {
        log.info("* HwRanInfoTrx4gCmTransferEngine onTransfer");
        List<String> items
                = networkItems.stream().map(NetworkItem::getItemCode).collect(Collectors.toList());

        ExecutorService executor = Executors.newFixedThreadPool(engineRecord.getOnTransferThreadCount());
        getConnections()
                .forEach(connection -> {
                    try {
                        executor.execute(
                                new HwRanInfoTrx4gCmTransferHandler(
                                        applicationContext,
                                        getTransferHandlerRecord(connection),
                                        items)
                        );
                    } catch (Exception exception) {
                        log.error("! HwRanInfoTrx4gCmTransferEngine onProcess connectionId:{} error: {}", connection.getId(),
                                exception.getMessage());
                    }
                });
        shutdownExecutorService(executor);
    }

    @Override
    protected ArrayList<File> getDecompressFiles() {
        return new ArrayList<>(fileLib.readFilesInCurrentPathByContains(engineRecord.getRawPath(), ".zip"));
    }

    @Override
    protected DecompressRecord getDecompressRecord(File file) {
        NetworkItem item
                = networkItems.stream()
                .filter(e -> e.getItemCode().equals(getItemName(file.getName())))
                .findFirst().get();

        return DecompressRecord.getRecord(engineRecord,
                file,
                OffsetDateTime.parse(
                        file.getName()
                                .split("_")[file.getName().split("_").length - 2]
                                + " 00:00+03:00",
                        DateTimeFormatter.ofPattern("yyyyMMdd HH:mmXXX")
                ),
                (file.getName().contains("^^") ? file.getName().split("\\^")[0] : null),
                null,
                file.getName(),
                item.getSourceStartsWith(),
                item.getSourceContains(),
                item.getSourceEndsWith());
    }

    public String getItemName(String fileName) {
        String name = (fileName.contains("^^") ? fileName.split("\\^")[2] : fileName);
        String[] split = name.split("_");
        return name.replace("_" + split[split.length - 2] + "_" + split[split.length - 1], "");
    }
}
