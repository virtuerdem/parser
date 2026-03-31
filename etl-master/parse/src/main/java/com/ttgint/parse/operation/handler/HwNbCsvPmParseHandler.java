package com.ttgint.parse.operation.handler;

import com.ttgint.library.record.ParseHandlerRecord;
import com.ttgint.library.record.ParseMapRecord;
import com.ttgint.parse.base.ParseCsvHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;

@Slf4j
public class HwNbCsvPmParseHandler extends ParseCsvHandler {

    private final HashMap<String, String> headerKeyValue = new HashMap<>();
    private final HashMap<Integer, String> indexKey = new HashMap<>();
    private final HashMap<String, String> keyValue = new HashMap<>();

    private String measInfo;
    private String itemCode;
    private ParseMapRecord parseMap;
    private boolean firstDataWritten;

    public HwNbCsvPmParseHandler(ApplicationContext applicationContext,
                                 ParseHandlerRecord handlerRecord) {
        super(applicationContext, handlerRecord, ',');
    }

    @Override
    public void preHandler() {
        String fileName = getHandlerRecord().getFile().getName();
        String originalName = fileName.contains("^^")
                ? fileName.split("\\^\\^")[1]
                : fileName;

        headerKeyValue.put("etlApp.info_fileId", fileName.split("\\^")[0]);

        String[] parts = originalName.split("_");
        itemCode = parts[2];
        measInfo = itemCode;

        String datePart = parts[parts.length - 2];
        headerKeyValue.put("etlApp.constant_fragmentDate",
                stringDateFormatter(datePart + " +03:00", "yyyyMMddHHmm XXX", "yyyy-MM-dd HH:mmZ"));

        parseMap = getParseMapper().getMapByObjectKey(measInfo);
        firstDataWritten = false;
    }

    @Override
    public void lineProgress(Long lineIndex, String[] line) {
        if (lineIndex == 0) {
            for (int i = 0; i < line.length; i++) {
                indexKey.put(i, line[i].trim());
            }
            return;
        }

        if (lineIndex == 1) return; // units row (e.g. ",Minutes,,,None,None,...")

        if (line.length == 0) return;

        keyValue.clear();
        for (int i = 0; i < indexKey.size(); i++) {
            keyValue.put(indexKey.get(i), getSafeIndex(line, i));
        }
        keyValue.put("etlApp.info_lineIndex", String.valueOf(lineIndex));
        prepareUniqueRowCode(keyValue);

        if (!firstDataWritten) {
            autoCounterDefine(null, null, itemCode, keyValue.keySet());
            firstDataWritten = true;
        }

        write();
    }

    @Override
    public void postHandler() {
        keyValue.clear();
        indexKey.clear();
        headerKeyValue.clear();
    }

    private void write() {
        keyValue.putAll(headerKeyValue);
        if (parseMap != null) {
            syncWriteIntoFile(parseMap, keyValue);
        } else {
            log.warn("! HwNbCsvPmParseHandler no parse map found for measInfo: {}", measInfo);
        }
    }
}
