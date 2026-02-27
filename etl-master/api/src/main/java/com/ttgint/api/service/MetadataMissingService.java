package com.ttgint.api.service;

import com.ttgint.library.model.*;
import com.ttgint.library.record.*;
import com.ttgint.library.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MetadataMissingService {

    private final AllCounterRepository allCounterRepository;
    private final AllTableRepository allTableRepository;
    private final AllColumnRepository allColumnRepository;
    private final AllPartitionRepository allPartitionRepository;
    private final AllIndexRepository allIndexRepository;
    private final ParseTableRepository parseTableRepository;
    private final ParseColumnRepository parseColumnRepository;

    public MetadataMissingService(AllCounterRepository allCounterRepository,
                                  AllTableRepository allTableRepository,
                                  AllColumnRepository allColumnRepository,
                                  AllPartitionRepository allPartitionRepository,
                                  AllIndexRepository allIndexRepository,
                                  ParseTableRepository parseTableRepository,
                                  ParseColumnRepository parseColumnRepository) {
        this.allCounterRepository = allCounterRepository;
        this.allTableRepository = allTableRepository;
        this.allColumnRepository = allColumnRepository;
        this.allPartitionRepository = allPartitionRepository;
        this.allIndexRepository = allIndexRepository;
        this.parseTableRepository = parseTableRepository;
        this.parseColumnRepository = parseColumnRepository;
    }

    public MetadataDefineFullTableReqRec getFullTable(
            MetadataMissingAllReqRec record) {
        MetadataDefineFullTableReqRec resp = new MetadataDefineFullTableReqRec();
        resp.setTable(getTable(record));
        resp.setColumn(getColumn(record));
        resp.setPartition(getPartition(record));
        resp.setIndex(getIndex(record));
        return resp;
    }

    public MetadataDefineAllTableReqRec getTable(
            MetadataMissingAllReqRec record) {
        if (record.getFlowId() == null || record.getCounterGroupKey() == null || record.getTimePeriod() == null) {
            return null;
        } else {
            boolean isPresent = findTable(record.getFlowId(), record.getElementType(), record.getCounterGroupType(),
                    record.getCounterGroupKey(), record.getTimePeriod()).isPresent();
            if (isPresent) return null;

            return allCounterRepository.findByFlowIdAndCounterGroupKey(record.getFlowId(), record.getCounterGroupKey())
                    .stream()
                    .filter(e -> (record.getElementType() == null || record.getElementType().equals(e.getElementType())) &&
                            (record.getCounterGroupType() == null || record.getCounterGroupType().equals(e.getCounterGroupType())))
                    .findAny()
                    .map(e -> new MetadataDefineAllTableReqRec(e, record.getTimePeriod()))
                    .get();
        }
    }

    public MetadataDefineAllColumnsReqRec getColumn(
            MetadataMissingAllReqRec record) {
        if (record.getFlowId() == null || record.getCounterGroupKey() == null || record.getTimePeriod() == null) {
            return null;
        } else {
            AllTable table
                    = findTable(record.getFlowId(), record.getElementType(), record.getCounterGroupType(),
                    record.getCounterGroupKey(), record.getTimePeriod())
                    .orElse(null);

            List<AllCounter> counters =
                    allCounterRepository.findByFlowIdAndCounterGroupKey(record.getFlowId(), record.getCounterGroupKey())
                            .stream()
                            .filter(e -> (record.getElementType() == null || record.getElementType().equals(e.getElementType())) &&
                                    (record.getCounterGroupType() == null || record.getCounterGroupType().equals(e.getCounterGroupType())))
                            .sorted(Comparator
                                    .comparing(AllCounter::getModelType)
                                    .thenComparing(AllCounter::getCounterKey))
                            .toList();

            List<String> objectKeys = new ArrayList<>();
            List<String> objectKeys2 = new ArrayList<>();

            MetadataDefineAllColumnsReqRec resp = new MetadataDefineAllColumnsReqRec(record.getFlowId());
            if (table != null) {
                resp.setAllTableId(table.getId());
                resp.setSchemaName(table.getSchemaName());
                resp.setTableName(table.getTableName());

                List<AllColumn> columns = allColumnRepository.findAllByAllTableId(table.getId());
                objectKeys.addAll(columns.stream().map(AllColumn::getObjectKey).toList());
                objectKeys2.addAll(columns.stream().map(AllColumn::getObjectKey2).toList());
            }

            resp.setColumns(
                    counters.stream()
                            .filter(e -> !objectKeys.contains(e.getCounterKey()) && !objectKeys2.contains(e.getCounterKey()))
                            .filter(e -> (record.getCounterKeys() == null
                                    || record.getCounterKeys().contains(e.getCounterKey())
                                    || !e.getModelType().equals("VARIABLE")))
                            .map(MetadataDefineAllColumnReqRec::new)
                            .toList()
            );

            if (resp.getColumns() == null || resp.getColumns().isEmpty()) {
                return null;
            }
            return resp;
        }
    }

    public MetadataDefineAllPartitionsReqRec getPartition(
            MetadataMissingAllReqRec record) {
        if (record.getFlowId() == null || record.getCounterGroupKey() == null || record.getTimePeriod() == null) {
            return null;
        } else {
            AllTable table
                    = findTable(record.getFlowId(), record.getElementType(), record.getCounterGroupType(),
                    record.getCounterGroupKey(), record.getTimePeriod())
                    .orElse(null);

            MetadataDefineAllPartitionsReqRec resp = new MetadataDefineAllPartitionsReqRec(record.getFlowId());
            if (table != null) {
                resp.setAllTableId(table.getId());
                resp.setSchemaName(table.getSchemaName());
                resp.setTableName(table.getTableName());
            }
            resp.setPartitions(List.of(new MetadataDefineAllPartitionReqRec(true)));
            return resp;
        }
    }

    public MetadataDefineAllIndexesReqRec getIndex(
            MetadataMissingAllReqRec record) {
        if (record.getFlowId() == null || record.getCounterGroupKey() == null || record.getTimePeriod() == null) {
            return null;
        } else {
            AllTable table
                    = findTable(record.getFlowId(), record.getElementType(), record.getCounterGroupType(),
                    record.getCounterGroupKey(), record.getTimePeriod())
                    .orElse(null);

            MetadataDefineAllIndexesReqRec resp = new MetadataDefineAllIndexesReqRec(record.getFlowId());
            if (table != null) {
                resp.setAllTableId(table.getId());
                resp.setSchemaName(table.getSchemaName());
                resp.setTableName(table.getTableName());
            }
            resp.setIndexes(List.of(new MetadataDefineAllIndexReqRec("fragment_date")));
            // todo check if exists
            return resp;
        }
    }

    public MetadataDefineParseMapReqRec getParseMap(
            MetadataMissingAllReqRec record) {
        MetadataDefineParseMapReqRec resp = new MetadataDefineParseMapReqRec();
        resp.setTable(getParseTable(record));
        resp.setColumns(getParseColumn(record));
        return resp;
    }

    public MetadataDefineParseTableReqRec getParseTable(
            MetadataMissingAllReqRec record) {
        if (record.getFlowId() == null || record.getCounterGroupKey() == null || record.getTimePeriod() == null) {
            return null;
        } else {
            AllTable table
                    = findTable(record.getFlowId(), record.getElementType(), record.getCounterGroupType(),
                    record.getCounterGroupKey(), record.getTimePeriod())
                    .orElseThrow(() -> new IllegalArgumentException("No AllTable to save for ParseTable"));

            boolean isPresent
                    = findParseTable(table.getId(), record.getCounterGroupKey(),
                    record.getElementType(), record.getCounterGroupType())
                    .isPresent();
            if (isPresent) return null;
            // todo check if exists
            return new MetadataDefineParseTableReqRec(table);
        }
    }

    public MetadataDefineParseColumnsReqRec getParseColumn(
            MetadataMissingAllReqRec record) {
        if (record.getFlowId() == null || record.getCounterGroupKey() == null || record.getTimePeriod() == null) {
            return null;
        } else {
            AllTable table
                    = findTable(record.getFlowId(), record.getElementType(), record.getCounterGroupType(),
                    record.getCounterGroupKey(), record.getTimePeriod())
                    .orElseThrow(() -> new IllegalArgumentException("No AllTable to save for ParseTable"));

            ParseTable parseTable
                    = findParseTable(table.getId(), record.getCounterGroupKey(),
                    record.getElementType(), record.getCounterGroupType())
                    .orElse(null);

            List<MetadataDefineParseColumnReqRec> parseColumns
                    = findColumns(table.getId())
                    .stream()
                    .sorted(Comparator.comparing(AllColumn::getColumnOrderId))
                    .map(MetadataDefineParseColumnReqRec::new)
                    .collect(Collectors.toList());

            MetadataDefineParseColumnsReqRec resp = new MetadataDefineParseColumnsReqRec(table);
            if (parseTable != null) {
                List<String> definedColumns
                        = parseColumnRepository.findAllByParseTableId(parseTable.getId())
                        .stream().map(ParseColumn::getColumnName).toList();
                resp.setColumns(parseColumns
                        .stream()
                        .filter(e -> !definedColumns.contains(e.getColumnName()))
                        .toList());

                if (resp.getColumns().isEmpty()) return null;
            } else {
                resp.setColumns(parseColumns);
            }
            return resp;
        }
    }

    public Optional<AllTable> findTable(Long flowId, String elementType, String objectType, String objectKey, Long timePeriod) {
        return allTableRepository.findByFlowIdAndTimePeriod(flowId, timePeriod)
                .stream()
                .filter(e -> (objectKey.equals(e.getObjectKey()) || objectKey.equals(e.getObjectKey2())) &&
                        (elementType == null || elementType.equals(e.getElementType())) &&
                        (objectType == null || objectType.equals(e.getObjectType()))
                ).sorted(Comparator
                        .comparing(AllTable::getFlowId)
                        .thenComparing(AllTable::getElementType)
                        .thenComparing(AllTable::getObjectType)
                        .thenComparing(AllTable::getObjectKey)
                        .thenComparing(AllTable::getObjectKey2)
                        .thenComparing(AllTable::getTableName)
                ).findFirst();
    }

    public List<AllColumn> findColumns(Long allTableId) {
        return allColumnRepository.findAllByAllTableId(allTableId);
    }

    public Optional<ParseTable> findParseTable(Long allTableId, String objectKey, String elementType, String objectType) {
        return parseTableRepository.findAllByAllTableIdAndObjectKey(allTableId, objectKey)
                .stream()
                .filter(e -> (elementType == null || elementType.equals(e.getElementType())) &&
                        (objectType == null || objectType.equals(e.getObjectType()))
                ).sorted(Comparator
                        .comparing(ParseTable::getFlowId)
                        .thenComparing(ParseTable::getElementType)
                        .thenComparing(ParseTable::getObjectType)
                        .thenComparing(ParseTable::getObjectKey)
                        .thenComparing(ParseTable::getTableName)
                ).findFirst();
    }
}
