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

    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mmZ");

    private final HashMap<String, String> headerKeyValue = new HashMap<>();
    private final HashMap<Integer, String> indexKey = new HashMap<>();
    private final HashMap<String, String> keyValue = new HashMap<>();

    private String scenarioType;
    private ParseMapRecord parseMap;

    public OthTwampPmCsvParseHandler(ApplicationContext applicationContext,
                                     ParseHandlerRecord handlerRecord) {
        super(applicationContext, handlerRecord, ',');
    }

    @Override
    public void preHandler() {
        String fileName = getHandlerRecord().getFile().getName();

        if (fileName.contains("^^")) {
            headerKeyValue.put("etlApp.info_fileId", fileName.split("\\^")[0]);
            fileName = fileName.split("\\^\\^")[1];
        }

        String baseName = fileName.replace(".csv", "");
        String timestamp = baseName.replaceAll(".*-(\\d{8}T\\d{6})$", "$1");
        scenarioType = baseName.replace("-" + timestamp, "");

        headerKeyValue.put("etlApp.constant_fragmentDate", parseTimestamp(timestamp));
        headerKeyValue.put("etlApp.constant_scenarioType", scenarioType);

        parseMap = getParseMapper().getMapByObjectKey(scenarioType);

        log.info("* OthTwampPmCsvParseHandler preHandler - scenarioType: {}, fragmentDate: {}",
                scenarioType, headerKeyValue.get("etlApp.constant_fragmentDate"));
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
                if (colName == null) {
                    continue;
                }
                String value = getSafeIndex(line, i);
                if ("TimeGroup".equals(colName)) {
                    value = parseTimestamp(value);
                }
                keyValue.put(colName, value);
            }
            keyValue.put("etlApp.info_lineIndex", String.valueOf(lineIndex));

            write();
            if (lineIndex == 1) {
                autoCounterDefine(null, null, scenarioType, keyValue.keySet());
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
            keyValue.putAll(prepareUniqueRowHashCode(parseMap, keyValue));
            prepareUniqueRowCode(keyValue);
            keyValue.putAll(prepareGeneratedValues(parseMap, keyValue));
            syncWriteIntoFile(parseMap, keyValue);
        }
    }

    private String parseTimestamp(String timestamp) {
        try {
            return LocalDateTime.parse(timestamp, INPUT_FORMATTER)
                    .atOffset(ZoneOffset.UTC)
                    .format(OUTPUT_FORMATTER);
        } catch (Exception e) {
            return timestamp;
        }
    }

}
