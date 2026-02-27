package com.ttgint.parse.operation.handler;

import com.ttgint.library.record.ParseHandlerRecord;
import com.ttgint.library.record.ParseMapRecord;
import com.ttgint.parse.base.ParseCsvHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;

@Slf4j
public class OthTwampCmCsvParseHandler extends ParseCsvHandler {

    private final HashMap<String, String> headerKeyValue = new HashMap<>();
    private final HashMap<Integer, String> indexKey = new HashMap<>();
    private final HashMap<String, String> keyValue = new HashMap<>();

    private String measInfo;
    private ParseMapRecord parseMap;

    public OthTwampCmCsvParseHandler(ApplicationContext applicationContext,
                                     ParseHandlerRecord handlerRecord,
                                     char splitter) {
        super(applicationContext, handlerRecord, splitter);
    }

    @Override
    public void preHandler() {
        String fileName = getHandlerRecord().getFile().getName();

        // Transfer'dan gelen dosyalarda fileId^^ prefix'i var, onu kaldır
        if (fileName.contains("^^")) {
            headerKeyValue.put("etlApp.info.fileId", fileName.split("\\^\\^")[0]);
            fileName = fileName.split("\\^\\^")[1];
        }

        // Dosya adı formatı: {measInfo}-{YYYY-MM-DD}.csv
        String[] parts = fileName.split("-", 2);
        measInfo = parts[0];

        headerKeyValue.put(
                "etlApp.constant.fragmentDate",
                stringDateFormatter(
                        parts[1].replace(".csv", "") + " 0000+03:00",
                        "yyyy-MM-dd HHmmXXX",
                        "yyyy-MM-dd HH:mmZ"
                )
        );

        parseMap = getParseMapper().getMapByObjectKey(measInfo);

        log.info("* OthTwampCmCsvParseHandler preHandler - measInfo: {}, parseMap: {}",
                measInfo, (parseMap != null ? "found" : "NOT FOUND"));
    }

    @Override
    public void lineProgress(Long lineIndex, String[] line) {
        if (lineIndex == 0) {
            // Header satırı - kolon isimlerini sakla
            for (int i = 0; i < line.length; i++) {
                indexKey.put(i, line[i].trim());
            }
        } else {
            // Data satırı
            keyValue.clear();
            for (int i = 0; i < indexKey.size(); i++) {
                String columnName = indexKey.get(i);
                String value = getSafeIndex(line, i);
                keyValue.put(columnName, value);
            }

            keyValue.put("etlApp.info.lineIndex", String.valueOf(lineIndex));

            // Counter tanımla (sadece ilk satırda)
            if (lineIndex == 1) {
                autoCounterDefine(null, null, measInfo, keyValue.keySet());
            }

            write();
        }
    }

    @Override
    public void postHandler() {
        keyValue.clear();
        indexKey.clear();
        headerKeyValue.clear();
    }

    private void write() {
        if (parseMap != null) {
            keyValue.putAll(headerKeyValue);

            // ⭐ ÖNEMLİ: Unique codes ve generated values ekle
            keyValue.putAll(prepareUniqueCodes(parseMap, keyValue));
            keyValue.putAll(prepareGeneratedValues(parseMap, keyValue));

            syncWriteIntoFile(parseMap, keyValue);
        }
    }

    private String getSafeIndex(String[] line, int index) {
        try {
            return line[index].replace("\t", " ").trim();
        } catch (ArrayIndexOutOfBoundsException e) {
            return "";
        }
    }
}
