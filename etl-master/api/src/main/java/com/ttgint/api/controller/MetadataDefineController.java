package com.ttgint.api.controller;

import com.ttgint.api.service.MetadataDefineService;
import com.ttgint.library.record.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/v1/metadata/define/")
public class MetadataDefineController {

    private final MetadataDefineService metadataDefineService;

    public MetadataDefineController(MetadataDefineService metadataDefineService) {
        this.metadataDefineService = metadataDefineService;
    }

    @PostMapping(value = "/counters")
    public ResponseEntity saveCounters(
            @RequestBody List<MetadataDefineAllCounterReqRec> record) {
        try {
            metadataDefineService.saveCounters(record);
            return ResponseEntity.ok(Map.of("message", "Counters created"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("message", "Error create Counters"));
        }
    }

    @PostMapping(value = "/table")
    public ResponseEntity saveTable(
            @RequestBody MetadataDefineFullTableReqRec record) {
        try {
            metadataDefineService.saveTable(record);
            return ResponseEntity.ok(Map.of("message", "Table created"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.ok(Map.of("message", "Error create Table"));
        }
    }

    @PostMapping(value = "/column")
    public ResponseEntity saveColumn(
            @RequestBody MetadataDefineAllColumnsReqRec record) {
        try {
            metadataDefineService.saveColumn(record);
            return ResponseEntity.ok(Map.of("message", "Columns created"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("message", "Error create Columns"));
        }
    }

    @PostMapping(value = "/partition")
    public ResponseEntity savePartition(
            @RequestBody MetadataDefineAllPartitionsReqRec record) {
        try {
            metadataDefineService.savePartition(record);
            return ResponseEntity.ok(Map.of("message", "Partitions created"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("message", "Error create Partitions"));
        }
    }

    @PostMapping(value = "/index")
    public ResponseEntity saveIndex(
            @RequestBody MetadataDefineAllIndexesReqRec record) {
        try {
            metadataDefineService.saveIndex(record);
            return ResponseEntity.ok(Map.of("message", "Indexes created"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("message", "Error create Indexes"));
        }
    }

    @PostMapping(value = "/parseTable")
    public ResponseEntity saveParseTable(
            @RequestBody MetadataDefineParseMapReqRec record) {
        try {
            metadataDefineService.saveParseTable(record);
            return ResponseEntity.ok(Map.of("message", "Parse Table created"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("message", "Error create Parse Table"));
        }
    }

    @PostMapping(value = "/parseColumns")
    public ResponseEntity saveParseColumns(
            @RequestBody MetadataDefineParseColumnsReqRec record) {
        try {
            metadataDefineService.saveParseColumn(record);
            return ResponseEntity.ok(Map.of("message", "Parse Columns created"));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("message", "Error create Parse Columns"));
        }
    }
}
