package com.ttgint.parse.operation.handler;

import com.ttgint.library.record.ParseHandlerRecord;
import com.ttgint.library.record.ParseMapRecord;
import com.ttgint.parse.base.ParseTxtHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;

@Slf4j
public class OthVasPmCsvParseHandler extends ParseTxtHandler {

    private final HashMap<String, String> headerKeyValue = new HashMap<>();
    private final HashMap<Integer, String> indexKey = new HashMap<>();
    private final HashMap<String, String> keyValue = new HashMap<>();

    private String measInfo;
    private ParseMapRecord parseMap;
    private boolean firstDataProcessed = false;

    public OthVasPmCsvParseHandler(ApplicationContext applicationContext,
                                   ParseHandlerRecord handlerRecord) {
        super(applicationContext, handlerRecord);
    }

    @Override
    public void preHandler() {
        String fileName = getHandlerRecord().getFile().getName();

        if (fileName.contains("^^")) {
            headerKeyValue.put("etlApp.info_fileId", fileName.split("\\^")[0]);
            String afterPrefix = fileName.split("\\^\\^")[1];
            String datePart = afterPrefix.split("_")[0];
            measInfo = afterPrefix.split("_", 2)[1].replace(".csv", "");
            headerKeyValue.put(
                    "etlApp.constant_fragmentDate",
                    stringDateFormatter(
                            datePart + " 0000+03:00",
                            "yyyyMMdd HHmmXXX",
                            "yyyy-MM-dd HH:mmZ"
                    )
            );
        } else {
            String datePart = fileName.split("_")[0];
            measInfo = fileName.split("_", 2)[1].replace(".csv", "");
            headerKeyValue.put(
                    "etlApp.constant_fragmentDate",
                    stringDateFormatter(
                            datePart + " 0000+03:00",
                            "yyyyMMdd HHmmXXX",
                            "yyyy-MM-dd HH:mmZ"
                    )
            );
        }

        parseMap = getParseMapper().getMapByObjectKey(measInfo);
    }

    @Override
    public void lineProgress(Long lineIndex, String line) {
        if (line == null || line.trim().isEmpty()) {
            return;
        }

        String[] parts = line.split("\\|\\|", -1);

        if (lineIndex == 0) {
            int dataIndex = 0;
            for (String part : parts) {
                String colName = part.trim();
                if (!colName.equals("'|'")) {
                    indexKey.put(dataIndex++, colName);
                }
            }
        } else {
            keyValue.clear();
            for (int i = 0; i < parts.length; i++) {
                String colName = indexKey.get(i);
                if (colName != null && !colName.isEmpty()) {
                    keyValue.put(colName, parts[i].trim());
                }
            }
            keyValue.put("etlApp.info_lineIndex", String.valueOf(lineIndex));
            prepareUniqueRowCode(keyValue);

            write();

            if (!firstDataProcessed) {
                firstDataProcessed = true;
                autoCounterDefine(null, null, measInfo, keyValue.keySet());
            }
        }
    }

    @Override
    public void postHandler() {
        keyValue.clear();
        indexKey.clear();
        headerKeyValue.clear();
        firstDataProcessed = false;
    }

    private void write() {
        keyValue.putAll(headerKeyValue);
        if (parseMap != null) {
            syncWriteIntoFile(parseMap, keyValue);
        }
    }
}
