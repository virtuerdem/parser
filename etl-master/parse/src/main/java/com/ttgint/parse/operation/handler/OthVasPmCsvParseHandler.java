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
        /*
         * Normal dosya (report12, report15, peak_report12, peak_report15, *_new):
         *   satır 0 → boş satır         → atla
         *   satır 1 → header            → indexKey doldur
         *   satır 2 → dashes (---...)   → atla
         *   satır 3+ → data
         *
         * Detail dosya (report_detail, report_detail_new):
         *   satır 0 → header            → indexKey doldur
         *   satır 1 → boş satır         → atla
         *   satır 2+ → data
         */

        long headerLine = isDetailFile ? 0L : 1L;

        // Header satırı
        if (lineIndex == headerLine) {
            for (int i = 0; i < line.length; i++) {
                // "||','||" kalıbındaki pipe ve quote karakterlerini temizle
                String colName = line[i].replaceAll("[|' ]", "").trim();
                indexKey.put(i, colName);
            }
            return;
        }

        // Normal dosya: satır 0 (boş) ve satır 2 (dashes) atla
        if (!isDetailFile && (lineIndex == 0L || lineIndex == 2L)) return;

        // Detail dosya: satır 1 (boş) atla
        if (isDetailFile && lineIndex == 1L) return;

        // Genel guard: boş satır veya dashes satırı atla
        if (line.length == 0) return;
        if (line.length == 1 && line[0].startsWith("---")) return;

        // Data satırı
        keyValue.clear();
        for (int i = 0; i < indexKey.size(); i++) {
            keyValue.put(indexKey.get(i), getSafeIndex(line, i));
        }
        keyValue.put("etlApp.info_lineIndex", String.valueOf(lineIndex));
        prepareUniqueRowCode(keyValue);

        // autoCounterDefine write'tan ÖNCE çağrılmalı
        if (!firstDataWritten) {
            autoCounterDefine(null, null, measInfo, keyValue.keySet());
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
            log.warn("! OthVasPmCsvParseHandler parseMap is null for measInfo: {}", measInfo);
        }
    }
}