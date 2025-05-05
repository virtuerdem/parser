package com.ttgint.library.metadata;

import com.ttgint.library.model.AllColumn;
import com.ttgint.library.model.AllIndex;
import com.ttgint.library.model.AllPartition;
import com.ttgint.library.model.AllTable;
import com.ttgint.library.nativeQuery.NativeQueryFactory;
import com.ttgint.library.record.*;
import com.ttgint.library.repository.AllColumnRepository;
import com.ttgint.library.repository.AllIndexRepository;
import com.ttgint.library.repository.AllPartitionRepository;
import com.ttgint.library.repository.AllTableRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class GenerateMetadata {

    private final GenerateMetadataRecord metadataRecord;

    private final AllTableRepository allTableRepository;
    private final AllColumnRepository allColumnRepository;
    private final AllIndexRepository allIndexRepository;
    private final AllPartitionRepository allPartitionRepository;

    private final NativeQueryFactory nativeQueryFactory;

    public GenerateMetadata(ApplicationContext applicationContext, GenerateMetadataRecord metadataRecord) {
        this.metadataRecord = metadataRecord;
        this.allTableRepository = applicationContext.getBean(AllTableRepository.class);
        this.allColumnRepository = applicationContext.getBean(AllColumnRepository.class);
        this.allIndexRepository = applicationContext.getBean(AllIndexRepository.class);
        this.allPartitionRepository = applicationContext.getBean(AllPartitionRepository.class);
        this.nativeQueryFactory = new NativeQueryFactory(applicationContext);
    }

    public void generate() {
        List<AllTable> nonExistsTables = getNonExistsTables(metadataRecord.getFlowId());
        List<AllColumn> definedColumns = getDefinedColumns(metadataRecord.getFlowId());
        List<AllIndex> definedIndexes = getDefinedIndexes(metadataRecord.getFlowId());
        AllPartition definedPartition = getDefinedPartition(metadataRecord.getFlowId());

        generateTables(nonExistsTables, definedColumns, definedIndexes, definedPartition);
        generateColumns(getNonExistsColumns(nonExistsTables, definedColumns));
    }

    public List<AllTable> getDefinedTables(Long flowId) {
        return allTableRepository.findAllByFlowIdAndIsActiveAndNeedRefreshAndIsGeneratedAndIsFailed(
                flowId, true, true, false, false);
    }

    public List<AllColumn> getDefinedColumns(Long flowId) {
        return allColumnRepository.findAllByFlowIdAndIsActiveAndNeedRefreshAndIsGeneratedAndIsFailed(
                flowId, true, true, false, false);
    }

    public List<AllPartition> getDefinedPartitions(Long flowId) {
        return allPartitionRepository.findAllByFlowIdAndIsActiveAndNeedRefreshAndIsGeneratedAndIsFailed(
                flowId, true, true, false, false);
    }

    public AllPartition getDefinedPartition(Long flowId) {
        return allPartitionRepository.findByFlowIdAndIsActiveAndNeedRefreshAndIsGeneratedAndIsFailed(
                flowId, true, true, false, false);
    }

    public List<AllIndex> getDefinedIndexes(Long flowId) {
        return allIndexRepository.findAllByFlowIdAndIsActiveAndNeedRefreshAndIsGeneratedAndIsFailed(
                flowId, true, true, false, false);
    }

    public String getSchemaNames(Stream<String> stream) {
        StringBuilder schemaNames = new StringBuilder();
        stream.map(e -> e.toLowerCase().trim())
                .distinct()
                .forEach(e -> schemaNames.append("','").append(e));
        return schemaNames.substring(3);
    }

    public List<AllTable> getNonExistsTables(Long flowId) {
        List<AllTable> definedTables = getDefinedTables(flowId);

        if (definedTables.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> existsTables =
                nativeQueryFactory.getNativeQuery().getExistsTables(
                        getSchemaNames(definedTables.stream().map(AllTable::getSchemaName)));

        return definedTables.stream()
                .filter(e -> !existsTables.contains(
                        (e.getSchemaName().trim() + "." + e.getTableName().trim()).toLowerCase()))
                .toList();
    }

    public List<AllColumn> getNonExistsColumns(List<AllTable> nonExistsTables, List<AllColumn> definedColumns) {
        List<String> nonExistsTableNames = nonExistsTables.stream()
                .map(e -> (e.getSchemaName().trim() + "." + e.getTableName().trim()).toLowerCase())
                .toList();

        List<AllColumn> definedColumnsFinal = definedColumns.stream()
                .filter(e -> !nonExistsTableNames.contains(
                        (e.getSchemaName().trim() + "." + e.getTableName().trim()).toLowerCase()))
                .toList();

        if (definedColumnsFinal.isEmpty()) {
            return new ArrayList<>();
        }

        List<String> existsColumns =
                nativeQueryFactory.getNativeQuery().getExistsColumns(
                        getSchemaNames(definedColumnsFinal.stream().map(AllColumn::getSchemaName)), null);

        return definedColumnsFinal.stream()
                .filter(e -> !existsColumns.contains(
                        (e.getSchemaName().trim() + "." + e.getTableName().trim() + "." + e.getColumnName().trim()).toLowerCase())
                ).toList();
    }

    public void generateTables(List<AllTable> nonExistsTables,
                               List<AllColumn> definedColumns,
                               List<AllIndex> definedIndexes,
                               AllPartition definedPartition) {
        HashMap<String, List<AllColumn>> tableColumns = new HashMap<>();
        for (AllColumn each : definedColumns) {
            if (tableColumns.containsKey((each.getSchemaName().trim() + "." + each.getTableName().trim()).toLowerCase())) {
                tableColumns.get((each.getSchemaName().trim() + "." + each.getTableName().trim()).toLowerCase()).add(each);
            } else {
                List<AllColumn> columns = new ArrayList<>();
                columns.add(each);
                tableColumns.put((each.getSchemaName().trim() + "." + each.getTableName().trim()).toLowerCase(), columns);
            }
        }

        for (AllTable each : nonExistsTables) {
            boolean status = generateTable(
                    GenerateTableRecord.getRecord(
                            each,
                            tableColumns.get((each.getSchemaName().trim() + "." + each.getTableName().trim()).toLowerCase()),
                            definedIndexes,
                            definedPartition
                    )
            );
            updateStatus(each.getId(), each.getSchemaName(), each.getTableName(), status);
        }
    }

    public void generateColumns(List<AllColumn> nonExistsColumns) {
        for (AllColumn each : nonExistsColumns) {
            boolean status = generateColumn(GenerateColumnRecord.getRecord(each));
            updateColumnStatus(each.getId(), status);
        }
    }

    public Boolean generateTable(GenerateTableRecord record) {
        return nativeQueryFactory.getNativeQuery().generateTable(record);
    }

    public Boolean generatePartition(GeneratePartitionRecord record) {
        return nativeQueryFactory.getNativeQuery().generatePartition(record);
    }

    public Boolean generateIndex(GenerateIndexRecord record) {
        return nativeQueryFactory.getNativeQuery().generateIndex(record);
    }

    public Boolean generateColumn(GenerateColumnRecord record) {
        return nativeQueryFactory.getNativeQuery().generateColumn(record);
    }

    public void updateStatus(Long id, String schemaName, String tableName, Boolean status) {
        updateTableStatus(id, status);
        updateColumnsStatus(schemaName, tableName, status);
        updateIndexesStatus(schemaName, tableName, status);
        updatePartitionsStatus(schemaName, tableName, status);
    }

    public void updateTableStatus(Long id, Boolean status) {
        allTableRepository.updateTableStatusById(id, OffsetDateTime.now(), false, status, !status);
    }

    public void updateColumnsStatus(String schemaName, String tableName, Boolean status) {
        allColumnRepository.updateColumnStatusBySchemaNameAndTableName(
                schemaName, tableName, OffsetDateTime.now(), false, status, !status);
    }

    public void updateColumnStatus(Long id, Boolean status) {
        allColumnRepository.updateColumnStatusById(id, OffsetDateTime.now(), false, status, !status);
    }

    public void updateIndexesStatus(String schemaName, String tableName, Boolean status) {
        allIndexRepository.updateIndexStatusBySchemaNameAndTableName(
                schemaName, tableName, OffsetDateTime.now(), false, status, !status);
    }

    public void updateIndexStatus(Long id, Boolean status) {
        allIndexRepository.updateIndexStatusById(id, OffsetDateTime.now(), false, status, !status);
    }

    public void updatePartitionsStatus(String schemaName, String tableName, Boolean status) {
        allPartitionRepository.updatePartitionStatusBySchemaNameAndTableName(
                schemaName, tableName, OffsetDateTime.now(), false, status, !status);
    }

    public void updatePartitionStatus(Long id, Boolean status) {
        allPartitionRepository.updatePartitionStatusById(id, OffsetDateTime.now(), false, status, !status);
    }


}
