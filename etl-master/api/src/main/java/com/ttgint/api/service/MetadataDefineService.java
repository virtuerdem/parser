package com.ttgint.api.service;

import com.ttgint.library.model.*;
import com.ttgint.library.record.*;
import com.ttgint.library.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class MetadataDefineService {

    private final AllTableRepository allTableRepository;
    private final AllColumnRepository allColumnRepository;
    private final AllPartitionRepository allPartitionRepository;
    private final AllIndexRepository allIndexRepository;
    private final ParseTableRepository parseTableRepository;
    private final ParseColumnRepository parseColumnRepository;

    public MetadataDefineService(AllTableRepository allTableRepository,
                                 AllColumnRepository allColumnRepository,
                                 AllPartitionRepository allPartitionRepository,
                                 AllIndexRepository allIndexRepository,
                                 ParseTableRepository parseTableRepository,
                                 ParseColumnRepository parseColumnRepository) {
        this.allTableRepository = allTableRepository;
        this.allColumnRepository = allColumnRepository;
        this.allPartitionRepository = allPartitionRepository;
        this.allIndexRepository = allIndexRepository;
        this.parseTableRepository = parseTableRepository;
        this.parseColumnRepository = parseColumnRepository;
    }

    public void saveCounters(List<MetadataDefineAllCounterReqRec> record) {
    }

    public void saveTable(MetadataDefineFullTableReqRec record) {
        MetadataDefineAllTableReqRec tableRec = record.getTable();

        Optional<AllTable> tab
                = findTable(tableRec.getFlowId(), tableRec.getElementType(), tableRec.getObjectType(),
                tableRec.getObjectKey(), tableRec.getTimePeriod());

        int allTableSize = getAllTableSize(tableRec.getFlowId());
        AllTable table
                = tab.orElseGet(() -> allTableRepository.save(new AllTable(tableRec, allTableSize + 1)));

        saveColumn(table.getFlowId(), table.getId(), table.getSchemaName(), table.getTableName(), record.getColumn());
        savePartition(table.getFlowId(), table.getId(), table.getSchemaName(), table.getTableName(), record.getPartition());
        saveIndex(table.getFlowId(), table.getId(), table.getSchemaName(), table.getTableName(), record.getIndex());
    }

    public void saveColumn(Long flowId,
                           Long allTableId,
                           String schemaName,
                           String tableName,
                           MetadataDefineAllColumnsReqRec record) {
        record.setFlowId(flowId);
        record.setAllTableId(allTableId);
        record.setSchemaName(schemaName);
        record.setTableName(tableName);
        saveColumn(record);
    }

    public void saveColumn(MetadataDefineAllColumnsReqRec record) {
        List<String> objectKeys = getAllColumns(record.getAllTableId()).stream().map(AllColumn::getObjectKey).toList();
        AtomicInteger columnSize = new AtomicInteger(objectKeys.size());
        allColumnRepository.saveAll(
                record.getColumns().stream()
                        .filter(e -> !objectKeys.contains(e.getObjectKey())
                                && !objectKeys.contains(e.getObjectKey2()))
                        .peek(this::setNonVariableColumnInfos)
                        .map(e ->
                                new AllColumn(
                                        record.getFlowId(),
                                        record.getAllTableId(),
                                        record.getSchemaName(),
                                        record.getTableName(),
                                        columnSize.incrementAndGet(),
                                        e
                                )
                        )
                        .toList()
        );
    }

    public void savePartition(Long flowId,
                              Long allTableId,
                              String schemaName,
                              String tableName,
                              MetadataDefineAllPartitionsReqRec record) {
        record.setFlowId(flowId);
        record.setAllTableId(allTableId);
        record.setSchemaName(schemaName);
        record.setTableName(tableName);
        savePartition(record);
    }

    public void savePartition(MetadataDefineAllPartitionsReqRec record) {
        List<String> partitionColumnName
                = getAllPartitions(record.getAllTableId()).stream().map(AllPartition::getPartitionColumnName).toList();
        allPartitionRepository.saveAll(
                record.getPartitions().stream()
                        .filter(e -> !partitionColumnName.contains(e.getPartitionColumnName()))
                        .map(e -> new AllPartition(
                                record.getFlowId(),
                                record.getAllTableId(),
                                record.getSchemaName(),
                                record.getTableName(),
                                e))
                        .toList()
        );
    }

    public void saveIndex(Long flowId,
                          Long allTableId,
                          String schemaName,
                          String tableName,
                          MetadataDefineAllIndexesReqRec record) {
        record.setFlowId(flowId);
        record.setAllTableId(allTableId);
        record.setSchemaName(schemaName);
        record.setTableName(tableName);
        saveIndex(record);
    }

    public void saveIndex(MetadataDefineAllIndexesReqRec record) {
        List<String> indexColumnName
                = getAllIndexes(record.getAllTableId()).stream().map(AllIndex::getIndexColumnName).toList();
        allIndexRepository.saveAll(
                record.getIndexes().stream()
                        .filter(e -> !indexColumnName.contains(e.getIndexColumnName()))
                        .map(e -> new AllIndex(
                                record.getFlowId(),
                                record.getAllTableId(),
                                record.getSchemaName(),
                                record.getTableName(),
                                e))
                        .toList()
        );
    }

    public void saveParseTable(MetadataDefineParseMapReqRec record) {
        Optional<ParseTable> tab
                = getParseTable(record.getTable().getAllTableId(), record.getTable().getObjectKey(),
                record.getTable().getElementType(), record.getTable().getObjectType());

        ParseTable table
                = tab.orElseGet(() -> parseTableRepository.save(new ParseTable(record.getTable())));
        saveParseColumn(table.getFlowId(), table.getId(), table.getSchemaName(), table.getTableName(), record.getColumns());
    }

    public void saveParseColumn(Long flowId,
                                Long parseTableId,
                                String schemaName,
                                String tableName,
                                MetadataDefineParseColumnsReqRec record) {
        record.setFlowId(flowId);
        record.setParseTableId(parseTableId);
        record.setSchemaName(schemaName);
        record.setTableName(tableName);
        saveParseColumn(record);
    }

    public void saveParseColumn(MetadataDefineParseColumnsReqRec record) {
        List<String> columnNames = getParseColumns(record.getParseTableId()).stream().map(ParseColumn::getColumnName).toList();
        parseColumnRepository.saveAll(
                record.getColumns().stream()
                        .filter(e -> !columnNames.contains(e.getColumnName()))
                        .map(e ->
                                new ParseColumn(
                                        record.getFlowId(),
                                        record.getParseTableId(),
                                        record.getSchemaName(),
                                        record.getTableName(),
                                        e
                                )
                        )
                        .toList()
        );
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

    public int getAllTableSize(Long flowId) {
        return allTableRepository.findAllByFlowId(flowId).size();
    }

    public List<AllColumn> getAllColumns(Long allTableId) {
        return allColumnRepository.findAllByAllTableId(allTableId);
    }

    public void setNonVariableColumnInfos(MetadataDefineAllColumnReqRec e) {
        if (e == null) {
            return;
        }
        if (!"VARIABLE".equals(e.getModelType())) {
            String objectKey = e.getObjectKey().split("_", 2)[1];
            e.setColumnName(e.getColumnName() != null ? e.getColumnName() :
                    objectKey
                            // camelCase / PascalCase → snake_case
                            .replaceAll("([a-z0-9])([A-Z])", "$1_$2")
                            .replaceAll("([A-Z]+)([A-Z][a-z])", "$1_$2")
                            // Replace any non-alphanumeric character with underscore
                            .replaceAll("[^a-zA-Z0-9]", "_")
                            // Collapse multiple consecutive underscores into a single underscore
                            .replaceAll("_+", "_")
                            // Remove leading and trailing underscores
                            .replaceAll("^_|_$", "")
                            .toLowerCase()
                            .trim()
            );

            boolean b = objectKey.equals("fragmentDate") || objectKey.equals("endTime") || objectKey.equals("beginTime");
            if (e.getColumnType() == null) {
                e.setColumnType(b ? "TIMESTAMPTZ" : "VARCHAR");
            }

            if (e.getColumnFormula() == null) {
                e.setColumnFormula(b ? "yyyy-mm-dd hh24:miZ" : null);
            }

            if (e.getColumnLength() == null) {
                switch (objectKey) {
                    case "duration":
                        e.setColumnLength(10);
                        break;
                    case "measObjLdn":
                        e.setColumnLength(1000);
                        break;
                    case "nodeId":
                    case "nodeName":
                    case "fileId":
                    case "uniqueRowCode":
                    case "uniqueRowHashCode":
                        e.setColumnLength(100);
                        break;
                }
                if ("MEASOBJLDN".equals(e.getModelType())) {
                    e.setColumnLength(100);
                }
            }
        } else {
            if (e.getColumnType() == null) {
                e.setColumnType("NUMERIC");
            }
        }
    }

    public List<AllPartition> getAllPartitions(Long allTableId) {
        return allPartitionRepository.findAllByAllTableId(allTableId);
    }

    public List<AllIndex> getAllIndexes(Long allTableId) {
        return allIndexRepository.findAllByAllTableId(allTableId);
    }

    public Optional<ParseTable> getParseTable(Long flowId, String objectKey, String elementType, String objectType) {
        return parseTableRepository.findAllByAllTableIdAndObjectKey(flowId, objectKey)
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

    public List<ParseColumn> getParseColumns(Long parseTableId) {
        return parseColumnRepository.findAllByParseTableId(parseTableId);
    }

}
