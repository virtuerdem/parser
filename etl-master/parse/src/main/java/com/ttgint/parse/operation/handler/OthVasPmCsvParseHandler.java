package com.ttgint.parse.operation.handler;

import com.ttgint.library.record.ParseHandlerRecord;
import com.ttgint.library.record.ParseMapRecord;
import com.ttgint.parse.base.ParseCsvHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;

@Slf4j
public class OthVasPmCsvParseHandler extends ParseCsvHandler {

    private final HashMap<String, String> headerKeyValue = new HashMap<>();
    private final HashMap<Integer, String> indexKey = new HashMap<>();
    private final HashMap<String, String> keyValue = new HashMap<>();

    private String measInfo;
    private ParseMapRecord parseMap;
    private boolean isDetailFile;
    private boolean firstDataWritten;

    public OthVasPmCsvParseHandler(ApplicationContext applicationContext,
                                   ParseHandlerRecord handlerRecord) {
        super(applicationContext, handlerRecord, ',');
    }

    @Override
    public void preHandler() {
        String fileName = getHandlerRecord().getFile().getName();
        String baseName = fileName.contains("^^")
                ? fileName.split("\\^\\^")[1]
                : fileName;

        String datePart = baseName.split("_")[0];
        measInfo = baseName.split("_", 2)[1].replace(".csv", "");
        isDetailFile = measInfo.contains("report_detail");
        firstDataWritten = false;

        headerKeyValue.put("etlApp.info_fileId", fileName.split("\\^")[0]);
        headerKeyValue.put("etlApp.constant_fragmentDate",
                stringDateFormatter(datePart + " 0000+03:00", "yyyyMMdd HHmmXXX", "yyyy-MM-dd HH:mmZ"));

        parseMap = getParseMapper().getMapByObjectKey(measInfo);
    }

    @Override
    public void lineProgress(Long lineIndex, String[] line) {
        if (line == null || line.length == 0 || (line.length == 1 && line[0].trim().isEmpty())) {
            return;
        }

        long headerLine = isDetailFile ? 0L : 1L;
        if (lineIndex == headerLine) {
            int dataIndex = 0;
            for (String col : line) {
                String colName = col.replaceAll("[|']", "").trim();
                if (!colName.isEmpty()) {
                    indexKey.put(dataIndex++, colName);
                }
            }
            return;
        }

        if (!isDetailFile && (lineIndex == 0 || lineIndex == 2)) return;

        keyValue.clear();
        for (int i = 0; i < indexKey.size(); i++) {
            keyValue.put(indexKey.get(i), getSafeIndex(line, i));
        }
        keyValue.put("etlApp.info_lineIndex", String.valueOf(lineIndex));
        prepareUniqueRowCode(keyValue);
        write();

        if (!firstDataWritten) {
            autoCounterDefine(null, null, measInfo, keyValue.keySet());
            firstDataWritten = true;
        }
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
            log.warn("! OthVasPmCsvParseHandler parseMap is null for measInfo: {}", measInfo);
        }
    }
}
