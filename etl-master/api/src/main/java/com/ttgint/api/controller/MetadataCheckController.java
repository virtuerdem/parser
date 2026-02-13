package com.ttgint.api.controller;

import com.ttgint.api.service.MetadataCheckService;
import com.ttgint.library.record.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/v1/metadata/check/")
public class MetadataCheckController {

    private final MetadataCheckService metadataCheckService;

    public MetadataCheckController(MetadataCheckService metadataCheckService) {
        this.metadataCheckService = metadataCheckService;
    }

    @GetMapping(value = "/counters")
    public ResponseEntity<List<MetadataCheckAllCountersRespRec>> getCounters(
            @RequestParam(required = false) Long flowId,
            @RequestParam(required = false) String counterGroupKey,
            @RequestParam(required = false) List<String> counterKeys) {
        return ResponseEntity.ok(metadataCheckService.getCounters(
                new MetadataCheckAllCounterReqRec(flowId, counterGroupKey, counterKeys)));
    }

    @GetMapping(value = "/table")
    public ResponseEntity<MetadataCheckFullTableRespRec> getFullTable(
            @RequestParam Long id) {
        return ResponseEntity.ok(metadataCheckService.getFullTable(id));
    }

    @GetMapping(value = "/tables")
    public ResponseEntity<List<MetadataCheckAllTableRespRec>> getTables(
            @RequestParam(required = false) Long flowId,
            @RequestParam(required = false) Long allTableId,
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) String objectKey,
            @RequestParam(required = false) String objectType,
            @RequestParam(required = false) String elementType) {
        return ResponseEntity.ok(metadataCheckService.getTables(
                new MetadataCheckAllTableReqRec(flowId, allTableId, tableName, objectKey, objectType, elementType)));
    }

    @GetMapping(value = "/columns")
    public ResponseEntity<List<MetadataCheckAllColumnRespRec>> getColumns(
            @RequestParam(required = false) Long flowId,
            @RequestParam(required = false) Long allTableId,
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) List<String> columnNames,
            @RequestParam(required = false) List<String> objectKeys) {
        return ResponseEntity.ok(metadataCheckService.getColumns(
                new MetadataCheckAllColumnReqRec(flowId, allTableId, tableName, columnNames, objectKeys)));
    }

    @GetMapping(value = "/partitions")
    public ResponseEntity<List<MetadataCheckPartitionRespRec>> getPartitions(
            @RequestParam(required = false) Long flowId,
            @RequestParam(required = false) Long allTableId,
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) List<String> partitionColumnNames) {
        return ResponseEntity.ok(metadataCheckService.getPartitions(
                new MetadataCheckAllPartitionReqRec(flowId, allTableId, tableName, partitionColumnNames)));
    }

    @GetMapping(value = "/indexes")
    public ResponseEntity<List<MetadataCheckAllIndexRespRec>> getIndexes(
            @RequestParam(required = false) Long flowId,
            @RequestParam(required = false) Long allTableId,
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) List<String> indexNames,
            @RequestParam(required = false) List<String> indexColumnNames) {
        return ResponseEntity.ok(metadataCheckService.getIndexes(
                new MetadataCheckAllIndexReqRec(flowId, allTableId, tableName, indexNames, indexColumnNames)));
    }

    @GetMapping(value = "/parseMap")
    public ResponseEntity<List<MetadataCheckParseMapRespRec>> getParseMap(
            @RequestParam Long allTableId) {
        return ResponseEntity.ok(metadataCheckService.getParseMap(allTableId));
    }

    @GetMapping(value = "/parseTables")
    public ResponseEntity<List<MetadataCheckParseTableRespRec>> getParseTables(
            @RequestParam(required = false) Long flowId,
            @RequestParam(required = false) Long allTableId,
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) String objectKey,
            @RequestParam(required = false) String objectType,
            @RequestParam(required = false) String elementType) {
        return ResponseEntity.ok(metadataCheckService.getParseTables(
                new MetadataCheckParseTableReqRec(flowId, allTableId, tableName, objectKey, objectType, elementType)));
    }

    @GetMapping(value = "/parseColumns")
    public ResponseEntity<List<MetadataCheckParseColumnRespRec>> getParseColumns(
            @RequestParam(required = false) Long flowId,
            @RequestParam(required = false) Long parseTableId,
            @RequestParam(required = false) String tableName,
            @RequestParam(required = false) List<String> columnNames,
            @RequestParam(required = false) List<String> objectKeys) {
        return ResponseEntity.ok(metadataCheckService.getParseColumns(
                new MetadataCheckParseColumnReqRec(flowId, parseTableId, tableName, columnNames, objectKeys)));
    }

}
