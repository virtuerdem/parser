package com.ttgint.parse.base;

import com.ezylang.evalex.Expression;
import com.ttgint.library.enums.ProgressType;
import com.ttgint.library.record.CounterDefineRecord;
import com.ttgint.library.record.ParseColumnRecord;
import com.ttgint.library.record.ParseHandlerRecord;
import com.ttgint.library.record.ParseMapRecord;
import com.ttgint.library.util.AutoCounterDefine;
import com.ttgint.library.util.ParseMapper;
import com.ttgint.library.util.Writer;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.openhft.hashing.LongHashFunction;
import org.springframework.context.ApplicationContext;
import org.xml.sax.helpers.DefaultHandler;

import java.nio.file.Files;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Getter
@Slf4j
public abstract class ParseBaseHandler extends DefaultHandler implements ParseHandler, Runnable {

    private final ApplicationContext applicationContext;
    private final ParseHandlerRecord handlerRecord;

    private final ParseMapper parseMapper;
    private final AutoCounterDefine autoCounterDefine;
    private final Writer writer;

    public ParseBaseHandler(ApplicationContext applicationContext, ParseHandlerRecord handlerRecord) {
        this.applicationContext = applicationContext;
        this.handlerRecord = handlerRecord;

        this.parseMapper = applicationContext.getBean(ParseMapper.class);
        this.autoCounterDefine = applicationContext.getBean(AutoCounterDefine.class);
        this.writer = applicationContext.getBean(Writer.class);
    }

    @Override
    public void run() {
        preHandler();
        onHandler();
        postHandler();
    }

    @Override
    public abstract void preHandler();

    @Override
    public abstract void onHandler();

    @Override
    public abstract void postHandler();

    @Override
    public void syncWriteIntoFile(String fileName, String line) {
        writer.sync(handlerRecord.getRawPath() + fileName + handlerRecord.getResultFileExtension(), line);
    }

    @Override
    public void syncWriteIntoFile(ParseMapRecord parseMap, HashMap<String, String> keyValue) {
        syncWriteIntoFile(parseMap.getParseTable().getTableName()
                        + "-" + parseMap.getParseTable().getObjectKey(),
                prepareRecord(parseMap, keyValue) + "\n");
    }

    @Override
    public void deleteFile(ProgressType progressType) {
        try {
            if (progressType.equals(ProgressType.PRODUCT)) {
                Files.delete(handlerRecord.getFile().toPath());
            }
        } catch (Exception exception) {
        }
    }

    @Override
    public String stringDateFormatter(String stringDate, String inputFormat, String outputFormat) {
        return OffsetDateTime
                .parse(stringDate, DateTimeFormatter.ofPattern(inputFormat))
                .format(DateTimeFormatter.ofPattern(outputFormat));
    }

    @Override
    public HashMap<String, String> prepareUniqueCodes(ParseMapRecord parseMap, HashMap<String, String> keyValue) {
        StringBuilder values = new StringBuilder();
        keyValue.keySet()
                .stream()
                .filter(e -> e.startsWith("etlApp.constant."))
                .sorted()
                .forEach(e -> values.append(keyValue.get(e))
                        .append(parseMap.getParseTable().getResultFileDelimiter()));

        HashMap<String, String> result = new HashMap<>();
        result.put("etlApp.info.uniqueRowHashCode", String.valueOf(LongHashFunction.xx().hashChars(values.toString())));
        result.put("etlApp.info.uniqueRowCode", UUID.randomUUID().toString());
        return result;
    }

    @Override
    public HashMap<String, String> prepareGeneratedValues(ParseMapRecord parseMap, HashMap<String, String> keyValue) {
        long cnt = parseMap.getParseColumns().stream().filter(ParseColumnRecord::getIsColumnGen).count();
        if (cnt > 0) {
            Map<String, Long> values = new HashMap<>();
            parseMap.getParseColumns()
                    .stream()
                    .filter(e -> e.getModelType().equals("VARIABLE"))
                    .forEach(c ->
                            values.put(c.getColumnName(),
                                    Long.valueOf(keyValue.getOrDefault(c.getObjectKey(), "0"))));

            HashMap<String, String> result = new HashMap<>();
            parseMap.getParseColumns()
                    .stream()
                    .filter(e -> e.getModelType().equals("VARIABLE"))
                    .filter(ParseColumnRecord::getIsColumnGen)
                    .forEach(c -> {
                        try {
                            result.put(c.getObjectKey(),
                                    new Expression(c.getColumnGenFormula().replace("greatest", "max"))
                                            .withValues(values)
                                            .evaluate()
                                            .getNumberValue()
                                            .toString());
                        } catch (Exception ex) {
                        }
                    });
            values.clear();
            return result;
        }
        return new HashMap<>();
    }

    @Override
    public String prepareRecord(ParseMapRecord parseMap, HashMap<String, String> keyValue) {
        StringBuilder stringBuilder = new StringBuilder();
        parseMap.getParseColumns()
                .stream()
                .sorted(Comparator.comparingInt(ParseColumnRecord::getColumnOrderId))
                .forEach(c ->
                        stringBuilder
                                .append(parseMap.getParseTable().getResultFileDelimiter())
                                .append(keyValue.getOrDefault(c.getObjectKey(),
                                                (c.getIsDefaultValue() ? c.getColumnDefaultValue() : "")
                                        )
                                )
                );
        return stringBuilder.substring(1);
    }

    public void autoCounterDefine(String nodeGroupType,
                                  String counterGroupType,
                                  String counterGroupKey,
                                  Set<String> counterKeys) {
        if (getHandlerRecord().getIsActiveAutoCounter()) {
            autoCounterDefine.collect(
                    counterKeys.stream()
                            .map(counterKey ->
                                    new CounterDefineRecord(nodeGroupType, counterGroupType, counterGroupKey, counterKey))
                            .toList()
            );
        }
    }

}
