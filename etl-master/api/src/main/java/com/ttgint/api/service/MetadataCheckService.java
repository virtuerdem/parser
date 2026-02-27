package com.ttgint.api.service;

import com.ttgint.library.model.*;
import com.ttgint.library.record.*;
import com.ttgint.library.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MetadataCheckService {

    private final AllCounterRepository allCounterRepository;
    private final AllTableRepository allTableRepository;
    private final AllColumnRepository allColumnRepository;
    private final AllPartitionRepository allPartitionRepository;
    private final AllIndexRepository allIndexRepository;
    private final ParseTableRepository parseTableRepository;
    private final ParseColumnRepository parseColumnRepository;

    public MetadataCheckService(AllCounterRepository allCounterRepository,
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

    public List<MetadataCheckAllCountersRespRec> getCounters(MetadataCheckAllCounterReqRec record) {
        List<String> counterKeys = record.getCounterKeys();
        if (record.getFlowId() == null && record.getCounterGroupKey() == null
                && (counterKeys == null || counterKeys.isEmpty())) {
            return null;
        } else {
            return allCounterRepository.findAll()
                    .stream()
                    .filter(e -> (record.getFlowId() == null || e.getFlowId().equals(record.getFlowId())) &&
                            (record.getCounterGroupKey() == null || e.getCounterGroupKey().equals(record.getCounterGroupKey())) &&
                            (counterKeys == null || counterKeys.isEmpty() || counterKeys.contains(e.getCounterKey()) ||
                                    (record.getCounterGroupKey() != null && !e.getModelType().equals("VARIABLE"))
                            )
                    ).sorted(Comparator
                            .comparing(AllCounter::getFlowId)
                            .thenComparing(AllCounter::getElementType)
                            .thenComparing(AllCounter::getCounterGroupType)
                            .thenComparing(AllCounter::getCounterGroupKey)
                            .thenComparing(AllCounter::getModelType)
                            .thenComparing(AllCounter::getCounterKey))
                    .collect(Collectors.groupingBy(
                            c -> c.getFlowId() + "|" +
                                    c.getElementType() + "|" +
                                    c.getCounterGroupType() + "|" +
                                    c.getCounterGroupKey()
                    ))
                    .values()
                    .stream()
                    .map(group -> {
                        AllCounter first = group.get(0);
                        MetadataCheckAllCountersRespRec resp = new MetadataCheckAllCountersRespRec();

                        resp.setFlowId(first.getFlowId());
                        resp.setElementType(first.getElementType());
                        resp.setCounterGroupType(first.getCounterGroupType());
                        resp.setCounterGroupKey(first.getCounterGroupKey());
                        resp.setCounterGroupLookup(first.getCounterGroupLookup());
                        resp.setCounterGroupDescription(first.getCounterGroupDescription());

                        List<MetadataCheckAllCounterRespRec> counters =
                                group.stream()
                                        .map(c -> {
                                            MetadataCheckAllCounterRespRec dto = new MetadataCheckAllCounterRespRec();
                                            dto.setCounterKey(c.getCounterKey());
                                            dto.setCounterLookup(c.getCounterLookup());
                                            dto.setCounterDescription(c.getCounterDescription());
                                            dto.setModelType(c.getModelType());
                                            dto.setIsActive(c.getIsActive());
                                            return dto;
                                        })
                                        .toList();

                        resp.setCounters(counters);
                        return resp;
                    })
                    .collect(Collectors.toList());
        }
    }

    public MetadataCheckFullTableRespRec getFullTable(Long id) {
        MetadataCheckAllTableRespRec table
                = allTableRepository.findById(id).map(MetadataCheckAllTableRespRec::new).orElse(null);

        if (table != null) {
            List<MetadataCheckAllColumnRespRec> columns
                    = allColumnRepository.findAllByAllTableId(id).stream()
                    .sorted(Comparator.comparing(AllColumn::getColumnOrderId))
                    .map(MetadataCheckAllColumnRespRec::new)
                    .collect(Collectors.toList());

            List<MetadataCheckPartitionRespRec> partitions
                    = allPartitionRepository.findAllByAllTableId(id).stream()
                    .sorted(Comparator.comparing(AllPartition::getPartitionColumnName))
                    .map(MetadataCheckPartitionRespRec::new)
                    .collect(Collectors.toList());

            List<MetadataCheckAllIndexRespRec> indexes
                    = allIndexRepository.findAllByAllTableId(id).stream()
                    .sorted(Comparator.comparing(AllIndex::getIndexName))
                    .map(MetadataCheckAllIndexRespRec::new)
                    .collect(Collectors.toList());

            List<MetadataCheckParseMapRespRec> parseMaps = getParseMap(id);

            MetadataCheckFullTableRespRec respRec = new MetadataCheckFullTableRespRec();
            respRec.setTable(table);
            respRec.setColumns(columns);
            respRec.setPartitions(partitions);
            respRec.setIndexes(indexes);
            respRec.setParseMaps(parseMaps);

            return respRec;
        } else {
            return null;
        }
    }

    public List<MetadataCheckAllTableRespRec> getTables(MetadataCheckAllTableReqRec record) {
        if (record.getFlowId() == null && record.getAllTableId() == null && record.getTableName() == null && record.getObjectKey() == null) {
            return null;
        } else {
            return allTableRepository.findAll().stream()
                    .filter(e -> (record.getFlowId() == null || e.getFlowId().equals(record.getFlowId())) &&
                            (record.getAllTableId() == null || e.getId().equals(record.getAllTableId())) &&
                            (record.getTableName() == null || e.getTableName().equals(record.getTableName())) &&
                            (record.getObjectKey() == null || e.getObjectKey().equals(record.getObjectKey())) &&
                            (record.getObjectKey() == null || e.getObjectKey2().equals(record.getObjectKey())) &&
                            (record.getObjectType() == null || e.getObjectType().equals(record.getObjectType())) &&
                            (record.getElementType() == null || e.getElementType().equals(record.getElementType()))
                    ).sorted(Comparator
                            .comparing(AllTable::getFlowId)
                            .thenComparing(AllTable::getElementType)
                            .thenComparing(AllTable::getObjectType)
                            .thenComparing(AllTable::getObjectKey)
                            .thenComparing(AllTable::getObjectKey2)
                            .thenComparing(AllTable::getTableName))
                    .map(MetadataCheckAllTableRespRec::new)
                    .collect(Collectors.toList());
        }
    }

    public List<MetadataCheckAllColumnRespRec> getColumns(MetadataCheckAllColumnReqRec record) {
        List<String> columnNames = record.getColumnNames();
        List<String> objectKeys = record.getObjectKeys();
        if (record.getFlowId() == null && record.getAllTableId() == null && record.getTableName() == null
                && (columnNames == null || columnNames.isEmpty())
                && (objectKeys == null || objectKeys.isEmpty())) {
            return null;
        } else {
            return allColumnRepository.findAll().stream()
                    .filter(e -> (record.getFlowId() == null || e.getFlowId().equals(record.getFlowId())) &&
                            (record.getAllTableId() == null || e.getAllTableId().equals(record.getAllTableId())) &&
                            (record.getTableName() == null || e.getTableName().equals(record.getTableName())) &&
                            (((record.getAllTableId() != null || record.getTableName() != null) && !e.getModelType().equals("VARIABLE"))
                                    || (columnNames == null || columnNames.isEmpty() || columnNames.contains(e.getColumnName()))
                                    || (objectKeys == null || objectKeys.isEmpty() || objectKeys.contains(e.getObjectKey()) || objectKeys.contains(e.getObjectKey2()))
                            )
                    ).sorted(Comparator
                            .comparing(AllColumn::getFlowId)
                            .thenComparing(AllColumn::getTableName)
                            .thenComparing(AllColumn::getColumnOrderId))
                    .map(MetadataCheckAllColumnRespRec::new)
                    .collect(Collectors.toList());
        }
    }

    public List<MetadataCheckPartitionRespRec> getPartitions(MetadataCheckAllPartitionReqRec record) {
        List<String> partitionColumnNames = record.getPartitionColumnNames();
        if (record.getFlowId() == null && record.getAllTableId() == null && record.getTableName() == null
                && (partitionColumnNames == null || partitionColumnNames.isEmpty())) {
            return null;
        } else {
            return allPartitionRepository.findAll().stream()
                    .filter(e -> (record.getFlowId() == null || e.getFlowId().equals(record.getFlowId())) &&
                            (record.getAllTableId() == null || e.getAllTableId().equals(record.getAllTableId())) &&
                            (record.getTableName() == null || e.getTableName().equals(record.getTableName())) &&
                            (partitionColumnNames == null || partitionColumnNames.isEmpty()
                                    || partitionColumnNames.contains(e.getPartitionColumnName()))
                    ).sorted(Comparator
                            .comparing(AllPartition::getFlowId)
                            .thenComparing(AllPartition::getTableName)
                            .thenComparing(AllPartition::getPartitionColumnName))
                    .map(MetadataCheckPartitionRespRec::new)
                    .collect(Collectors.toList());
        }
    }

    public List<MetadataCheckAllIndexRespRec> getIndexes(MetadataCheckAllIndexReqRec record) {
        List<String> indexNames = record.getIndexNames();
        List<String> indexColumnNames = record.getIndexColumnNames();
        if (record.getFlowId() == null && record.getAllTableId() == null && record.getTableName() == null
                && (indexNames == null || indexNames.isEmpty())
                && (indexColumnNames == null || indexColumnNames.isEmpty())) {
            return null;
        } else {
            return allIndexRepository.findAll().stream()
                    .filter(e -> (record.getFlowId() == null || e.getFlowId().equals(record.getFlowId())) &&
                            (record.getAllTableId() == null || e.getAllTableId().equals(record.getAllTableId())) &&
                            (record.getTableName() == null || e.getTableName().equals(record.getTableName())) &&
                            (indexNames == null || indexNames.isEmpty() || indexNames.contains(e.getIndexName())) &&
                            (indexColumnNames == null || indexColumnNames.isEmpty() || indexColumnNames.contains(e.getIndexColumnName()))
                    ).sorted(Comparator
                            .comparing(AllIndex::getFlowId)
                            .thenComparing(AllIndex::getTableName)
                            .thenComparing(AllIndex::getIndexName))
                    .map(MetadataCheckAllIndexRespRec::new)
                    .collect(Collectors.toList());
        }
    }

    public List<MetadataCheckParseMapRespRec> getParseMap(Long allTableId) {
        List<MetadataCheckParseMapRespRec> resp = new ArrayList<>();
        List<MetadataCheckParseTableRespRec> tables
                = parseTableRepository.findAllByAllTableId(allTableId)
                .stream()
                .sorted(Comparator.comparing(ParseTable::getObjectKey)
                        .thenComparing(ParseTable::getTableName)
                )
                .map(MetadataCheckParseTableRespRec::new)
                .toList();
        tables.forEach(e -> {
            MetadataCheckParseMapRespRec rec = new MetadataCheckParseMapRespRec();
            rec.setParseTable(e);
            rec.setParseColumns(
                    parseColumnRepository.findAllByParseTableId(e.getParseTableId())
                            .stream()
                            .sorted(Comparator.comparing(ParseColumn::getColumnOrderId))
                            .map(MetadataCheckParseColumnRespRec::new)
                            .toList());
        });
        return resp;
    }

    public List<MetadataCheckParseTableRespRec> getParseTables(MetadataCheckParseTableReqRec record) {
        if (record.getFlowId() == null && record.getAllTableId() == null && record.getTableName() == null && record.getObjectKey() == null) {
            return null;
        } else {
            return parseTableRepository.findAll().stream()
                    .filter(e -> ((record.getAllTableId() == null || e.getId().equals(record.getAllTableId())) &&
                            (record.getTableName() == null || e.getTableName().equals(record.getTableName())) &&
                            (record.getObjectKey() == null || e.getObjectKey().equals(record.getObjectKey())) &&
                            (record.getObjectType() == null || e.getObjectType().equals(record.getObjectType())) &&
                            (record.getElementType() == null || e.getElementType().equals(record.getElementType())))
                    ).sorted(Comparator
                            .comparing(ParseTable::getFlowId)
                            .thenComparing(ParseTable::getElementType)
                            .thenComparing(ParseTable::getObjectType)
                            .thenComparing(ParseTable::getObjectKey)
                            .thenComparing(ParseTable::getTableName))
                    .map(MetadataCheckParseTableRespRec::new)
                    .collect(Collectors.toList());
        }
    }

    public List<MetadataCheckParseColumnRespRec> getParseColumns(MetadataCheckParseColumnReqRec record) {
        List<String> columnNames = record.getColumnNames();
        List<String> objectKeys = record.getObjectKeys();
        if (record.getFlowId() == null && record.getParseTableId() == null && record.getTableName() == null
                && (columnNames == null || columnNames.isEmpty())
                && (objectKeys == null || objectKeys.isEmpty())) {
            return null;
        } else {
            return parseColumnRepository.findAll().stream()
                    .filter(e -> (record.getFlowId() == null || e.getFlowId().equals(record.getFlowId())) &&
                            (record.getTableName() == null || e.getTableName().equals(record.getTableName())) &&
                            (record.getParseTableId() == null || e.getParseTableId().equals(record.getParseTableId())) &&
                            (((record.getParseTableId() != null || record.getTableName() != null) && !e.getModelType().equals("VARIABLE"))
                                    || (columnNames == null || columnNames.isEmpty() || columnNames.contains(e.getColumnName()))
                                    || (objectKeys == null || objectKeys.isEmpty() || objectKeys.contains(e.getObjectKey()))
                            )
                    ).sorted(Comparator
                            .comparing(ParseColumn::getFlowId)
                            .thenComparing(ParseColumn::getTableName)
                            .thenComparing(ParseColumn::getColumnOrderId))
                    .map(MetadataCheckParseColumnRespRec::new)
                    .collect(Collectors.toList());
        }
    }

}
