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

    private final HashMap<Integer, String> columnIndexMap = new HashMap<>();
    private final HashMap<String, String> keyValue = new HashMap<>();

    private String scenarioType;
    private String fragmentDate;

    public OthTwampPmCsvParseHandler(ApplicationContext applicationContext,
                                     ParseHandlerRecord handlerRecord) {
        super(applicationContext, handlerRecord, ',');
    }

    @Override
    public void preHandler() {
        String fileName = getHandlerRecord().getFile().getName();

        // Transfer'dan gelen dosyalarda fileId^^ prefix'i var, onu kaldır
        if (fileName.contains("^^")) {
            fileName = fileName.split("\\^\\^")[1];
        }

        String baseName = fileName.replace(".csv", "");
        // dosya adi: test-20250624T123000  veya  test-dns-20250624T123000
        String timestamp = baseName.replaceAll(".*-(\\d{8}T\\d{6})$", "$1");
        scenarioType = baseName.replace("-" + timestamp, "");
        fragmentDate = parseTimestamp(timestamp);

        log.info("* OthTwampPmCsvParseHandler preHandler - scenarioType: {}, fragmentDate: {}",
                scenarioType, fragmentDate);
    }

    @Override
    public void lineProgress(Long lineIndex, String[] line) {
        if (lineIndex == 0) {
            for (int i = 0; i < line.length; i++) {
                columnIndexMap.put(i, line[i].trim());
            }
            return;
        }

        keyValue.clear();
        keyValue.put("etlApp.constant.fragmentDate", fragmentDate);
        keyValue.put("etlApp.constant.scenarioType", scenarioType);

        for (int i = 0; i < line.length; i++) {
            String colName = columnIndexMap.get(i);
            if (colName == null) {
                continue;
            }
            String value = line[i].replace("\t", " ").trim();
            if ("TimeGroup".equals(colName)) {
                value = parseTimestamp(value);
            }
            keyValue.put("etlApp.csv." + colName, value);
        }

        ParseMapRecord parseMap = getParseMapper().getMapByObjectKey(scenarioType);
        if (parseMap != null) {
            keyValue.putAll(prepareUniqueRowHashCode(parseMap, keyValue));
            prepareUniqueRowCode(keyValue);
            keyValue.putAll(prepareGeneratedValues(parseMap, keyValue));
            syncWriteIntoFile(parseMap, keyValue);
            if (lineIndex == 1) {
                autoCounterDefine(null, null, scenarioType, keyValue.keySet());
            }
        } else if (lineIndex == 1) {
            // Sadece ilk data satırında uyar (spam önlemek için)
            log.warn("! OthTwampPmCsvParseHandler parseMap not found for scenarioType: {}", scenarioType);
        }
    }

    @Override
    public void postHandler() {
        columnIndexMap.clear();
        keyValue.clear();
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