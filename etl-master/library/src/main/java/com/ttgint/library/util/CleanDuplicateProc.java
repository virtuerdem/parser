package com.ttgint.library.util;

import com.ttgint.library.nativeQuery.NativeQueryFactory;
import com.ttgint.library.record.StoredProcedureParamRecord;
import com.ttgint.library.repository.ParseTableRepository;
import jakarta.persistence.ParameterMode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CleanDuplicateProc {

    protected final NativeQueryFactory nativeQueryFactory;
    protected final ParseTableRepository parseTableRepository;

    public CleanDuplicateProc(ApplicationContext applicationContext, ParseTableRepository parseTableRepository) {
        this.nativeQueryFactory = new NativeQueryFactory(applicationContext);
        this.parseTableRepository = parseTableRepository;
    }

    public void cleanDuplicateProc(
            Long flowId,
            String flowCode,
            String schemaName,
            String version,
            Integer threadCount,
            List<OffsetDateTime> contentDates) {
        log.info("* ParseBaseEngine cleanDuplicateProc version: {}{}", version, "v5".equals(version) || "v6".equals(version)
                ? (" threadCount: " + (threadCount == null ? 1 : threadCount)) : "");

        List<String> parseTable
                = parseTableRepository.findAllByFlowIdAndIsActive(flowId, true)
                .stream().map(e -> e.getSchemaName() + "." + e.getTableName())
                .distinct().sorted().toList();

        if (version == null) {
            version = "v0";
            log.error("! CleanDuplicateProc version is null");
        }
        if (threadCount == null) {
            threadCount = 1;
        }
        for (OffsetDateTime fragmentDate : contentDates) {
            switch (version) {
                case "v6": // v4 with parallel threads
                    cleanDuplicateProcV6(flowId, flowCode, schemaName, fragmentDate, parseTable, threadCount);
                    break;
                case "v5": // v3 with parallel threads
                    cleanDuplicateProcV5(flowId, flowCode, schemaName, fragmentDate, parseTable, threadCount);
                    break;
                case "v4": // table based 'unique_row_hash_code' column
                    cleanDuplicateProcV4(flowId, flowCode, schemaName, fragmentDate, parseTable);
                    break;
                case "v3": // table based constant columns
                    cleanDuplicateProcV3(flowId, flowCode, schemaName, fragmentDate, parseTable);
                    break;
                case "v2": // flow based 'unique_row_hash_code' column
                    cleanDuplicateProcV2(flowId, flowCode, schemaName, fragmentDate);
                    break;
                case "v1": // flow based constant columns
                    cleanDuplicateProcV1(flowId, flowCode, schemaName, fragmentDate);
                    break;
            }
        }
    }

    private TreeMap<Integer, StoredProcedureParamRecord> getParam(Long flowId, String flowCode, OffsetDateTime fragmentDate) {
        TreeMap<Integer, StoredProcedureParamRecord> params = new TreeMap<>();
        params.put(1, new StoredProcedureParamRecord(Long.class, ParameterMode.IN, flowId));
        params.put(2, new StoredProcedureParamRecord(String.class, ParameterMode.IN, flowCode));
        params.put(3, new StoredProcedureParamRecord(OffsetDateTime.class, ParameterMode.IN, fragmentDate));
        return params;
    }

    private TreeMap<Integer, StoredProcedureParamRecord> getParam(Long flowId, String flowCode, OffsetDateTime fragmentDate, String table) {
        TreeMap<Integer, StoredProcedureParamRecord> params = getParam(flowId, flowCode, fragmentDate);
        params.put(4, new StoredProcedureParamRecord(String.class, ParameterMode.IN, table));
        return params;
    }

    private void cleanDuplicateProcV1(Long flowId, String flowCode, String schemaName, OffsetDateTime fragmentDate) {
        nativeQueryFactory.getNativeQuery().executeStoredProcedure(
                schemaName + ".p_clean_duplicate_v1",
                getParam(flowId, flowCode, fragmentDate),
                "* cleanDuplicateProcV1 for " + fragmentDate);
    }

    private void cleanDuplicateProcV2(Long flowId, String flowCode, String schemaName, OffsetDateTime fragmentDate) {
        nativeQueryFactory.getNativeQuery().executeStoredProcedure(
                schemaName + ".p_clean_duplicate_v2",
                getParam(flowId, flowCode, fragmentDate),
                "* cleanDuplicateProcV2 for " + fragmentDate);
    }

    private void cleanDuplicateProcV3(Long flowId, String flowCode, String schemaName, OffsetDateTime fragmentDate, List<String> parseTable) {
        for (String table : parseTable) {
            nativeQueryFactory.getNativeQuery().executeStoredProcedure(
                    schemaName + ".p_clean_duplicate_v3",
                    getParam(flowId, flowCode, fragmentDate, table),
                    null);
        }
    }

    private void cleanDuplicateProcV4(Long flowId, String flowCode, String schemaName, OffsetDateTime fragmentDate, List<String> parseTable) {
        for (String table : parseTable) {
            nativeQueryFactory.getNativeQuery().executeStoredProcedure(
                    schemaName + ".p_clean_duplicate_v4",
                    getParam(flowId, flowCode, fragmentDate, table),
                    null);
        }
    }

    private void cleanDuplicateProcV5(Long flowId, String flowCode, String schemaName, OffsetDateTime fragmentDate, List<String> parseTable, Integer threadCount) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (String table : parseTable) {
            executor.execute(() ->
                nativeQueryFactory.getNativeQuery().executeStoredProcedure(
                        schemaName + ".p_clean_duplicate_v5",
                        getParam(flowId, flowCode, fragmentDate, table),
                        null)
            );
        }
        shutdownExecutorService(executor);
    }

    private void cleanDuplicateProcV6(Long flowId, String flowCode, String schemaName, OffsetDateTime fragmentDate, List<String> parseTable, Integer threadCount) {
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        for (String table : parseTable) {
            executor.execute(() ->
                nativeQueryFactory.getNativeQuery().executeStoredProcedure(
                        schemaName + ".p_clean_duplicate_v6",
                        getParam(flowId, flowCode, fragmentDate, table),
                        null)
            );
        }
        shutdownExecutorService(executor);
    }

    private void shutdownExecutorService(ExecutorService executor) {
        try {
            executor.shutdown();
            while (!executor.awaitTermination(100, TimeUnit.MILLISECONDS)) {
                Thread.sleep(1000);
            }
        } catch (Exception exception) {
        }
    }
}
