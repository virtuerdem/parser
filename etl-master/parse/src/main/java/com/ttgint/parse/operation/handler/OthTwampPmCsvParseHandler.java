package com.ttgint.parse.operation.handler;

import com.ttgint.library.record.ParseHandlerRecord;
import com.ttgint.library.record.ParseMapRecord;
import com.ttgint.parse.base.ParseCsvHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

@Slf4j
public class OthTwampPmCsvParseHandler extends ParseCsvHandler {

    private static final DateTimeFormatter INPUT_FMT  = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private static final DateTimeFormatter OUTPUT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mmZ");

    private final HashMap<String, String> headerKeyValue = new HashMap<>();
    private final HashMap<Integer, String> indexKey = new HashMap<>();
    private final HashMap<String, String> keyValue = new HashMap<>();

    private String measInfo;
    private ParseMapRecord parseMap;

    public OthTwampPmCsvParseHandler(ApplicationContext applicationContext,
                                     ParseHandlerRecord handlerRecord) {
        super(applicationContext, handlerRecord, ',');
    }

    @Override
    public void preHandler() {
        String fileName = getHandlerRecord().getFile().getName();

        if (fileName.contains("^^")) {
            String[] parts = fileName.split("\\^\\^");
            headerKeyValue.put("etlApp.info_fileId", parts[0]);
            measInfo = parts[1].replaceAll("-(\\d{8}T\\d{6})\\.csv$", "");
        } else {
            measInfo = fileName.replaceAll("-(\\d{8}T\\d{6})\\.csv$", "");
        }

        String timestamp = fileName.replaceAll(".*-(\\d{8}T\\d{6})\\.csv$", "$1");
        headerKeyValue.put("etlApp.constant_fragmentDate", parseTimestamp(timestamp));

        parseMap = getParseMapper().getMapByObjectKey(measInfo);
    }

    @Override
    public void lineProgress(Long lineIndex, String[] line) {
        if (lineIndex == 0) {
            for (int i = 0; i < line.length; i++) {
                indexKey.put(i, line[i].trim());
            }
        } else {
            keyValue.clear();
            for (int i = 0; i < indexKey.size(); i++) {
                String colName = indexKey.get(i);
                String value = getSafeIndex(line, i);
                if ("TimeGroup".equals(colName) && !value.isEmpty()) {
                    value = parseTimestamp(value);
                }
                keyValue.put(colName, value);
            }

            keyValue.put("etlApp.info_lineIndex", String.valueOf(lineIndex));
            prepareUniqueRowCode(keyValue);

            write();

            if (lineIndex == 1) {
                autoCounterDefine(null, null, measInfo, keyValue.keySet());
            }
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
        }
    }

    private String parseTimestamp(String raw) {
        try {
            return LocalDateTime.parse(raw, INPUT_FMT)
                    .atOffset(ZoneOffset.of("+03:00"))
                    .format(OUTPUT_FMT);
        } catch (Exception e) {
            log.warn("! OthTwampPmCsvParseHandler parseTimestamp failed for: {}", raw);
            return raw;
        }
    }
}