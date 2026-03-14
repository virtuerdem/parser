package com.ttgint.parse.operation.handler;

import com.ttgint.library.record.ParseHandlerRecord;
import com.ttgint.library.record.ParseMapRecord;
import com.ttgint.parse.base.ParseCsvHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;

@Slf4j
public class HwMwCsvPmParseHandler extends ParseCsvHandler {

    private final HashMap<String, String> headerKeyValue = new HashMap<>();
    private final HashMap<Integer, String> indexKey = new HashMap<>();
    private final HashMap<String, String> keyValue = new HashMap<>();

    private String measInfo;
    private ParseMapRecord parseMap;
    private boolean headerFound;
    private boolean firstDataWritten;

    public HwMwCsvPmParseHandler(ApplicationContext applicationContext,
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
        String datePart = parts[parts.length - 2];

        // item code is everything except the last two underscore-separated parts (_date_HH-mm-ss.csv)
        measInfo = originalName.replace("_" + datePart + "_" + parts[parts.length - 1], "");

        String dateFormat = (datePart.split("-")[0].length() == 4 ? "yyyy-MM-dd" : "MM-dd-yyyy") + " HH:mmXXX";
        headerKeyValue.put("etlApp.constant_fragmentDate",
                stringDateFormatter(datePart + " 00:00+03:00", dateFormat, "yyyy-MM-dd HH:mmZ"));

        parseMap = getParseMapper().getMapByObjectKey(measInfo);
        headerFound = false;
        firstDataWritten = false;
    }

    @Override
    public void lineProgress(Long lineIndex, String[] line) {
        // File format: metadata block (lines 0-10), then column header, then data rows.
        // Detect header as first line with more than one column.
        if (!headerFound) {
            if (line.length > 1) {
                for (int i = 0; i < line.length; i++) {
                    indexKey.put(i, line[i].trim().replace("\"", ""));
                }
                headerFound = true;
            }
            return;
        }

        if (line.length == 0 || (line.length == 1 && line[0].trim().isEmpty())) return;

        keyValue.clear();
        for (int i = 0; i < indexKey.size(); i++) {
            keyValue.put(indexKey.get(i), getSafeIndex(line, i));
        }
        keyValue.put("etlApp.info_lineIndex", String.valueOf(lineIndex));
        prepareUniqueRowCode(keyValue);

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
            log.warn("! HwMwCsvPmParseHandler no parse map found for measInfo: {}", measInfo);
        }
    }
}
