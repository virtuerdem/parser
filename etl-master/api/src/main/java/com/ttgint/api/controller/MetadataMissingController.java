package com.ttgint.api.controller;

import com.ttgint.api.service.MetadataMissingService;
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
@RequestMapping("/v1/metadata/missing/")
public class MetadataMissingController {

    private final MetadataMissingService metadataMissingService;

    public MetadataMissingController(MetadataMissingService metadataMissingService) {
        this.metadataMissingService = metadataMissingService;
    }

    @GetMapping(value = "/tables")
    public ResponseEntity<MetadataDefineFullTableReqRec> getFullTable(
            @RequestParam Long flowId,
            @RequestParam(required = false) String elementType,
            @RequestParam(required = false) String counterGroupType,
            @RequestParam String counterGroupKey,
            @RequestParam Long timePeriod,
            @RequestParam(required = false) List<String> counterKeys) {
        return ResponseEntity.ok(metadataMissingService.getFullTable(
                new MetadataMissingAllReqRec(flowId, elementType, counterGroupType, counterGroupKey, timePeriod, counterKeys)));
    }

    @GetMapping(value = "/table")
    public ResponseEntity<MetadataDefineAllTableReqRec> getTable(
            @RequestParam Long flowId,
            @RequestParam(required = false) String elementType,
            @RequestParam(required = false) String counterGroupType,
            @RequestParam String counterGroupKey,
            @RequestParam Long timePeriod,
            @RequestParam(required = false) List<String> counterKeys) {
        return ResponseEntity.ok(metadataMissingService.getTable(
                new MetadataMissingAllReqRec(flowId, elementType, counterGroupType, counterGroupKey, timePeriod, counterKeys)));
    }

    @GetMapping(value = "/column")
    public ResponseEntity<MetadataDefineAllColumnsReqRec> getColumn(
            @RequestParam Long flowId,
            @RequestParam(required = false) String elementType,
            @RequestParam(required = false) String counterGroupType,
            @RequestParam String counterGroupKey,
            @RequestParam Long timePeriod,
            @RequestParam(required = false) List<String> counterKeys) {
        return ResponseEntity.ok(metadataMissingService.getColumn(
                new MetadataMissingAllReqRec(flowId, elementType, counterGroupType, counterGroupKey, timePeriod, counterKeys)));
    }

    @GetMapping(value = "/partition")
    public ResponseEntity<MetadataDefineAllPartitionsReqRec> getPartition(
            @RequestParam Long flowId,
            @RequestParam(required = false) String elementType,
            @RequestParam(required = false) String counterGroupType,
            @RequestParam String counterGroupKey,
            @RequestParam Long timePeriod,
            @RequestParam(required = false) List<String> counterKeys) {
        return ResponseEntity.ok(metadataMissingService.getPartition(
                new MetadataMissingAllReqRec(flowId, elementType, counterGroupType, counterGroupKey, timePeriod, counterKeys)));
    }

    @GetMapping(value = "/index")
    public ResponseEntity<MetadataDefineAllIndexesReqRec> getIndex(
            @RequestParam Long flowId,
            @RequestParam(required = false) String elementType,
            @RequestParam(required = false) String counterGroupType,
            @RequestParam String counterGroupKey,
            @RequestParam Long timePeriod,
            @RequestParam(required = false) List<String> counterKeys) {
        return ResponseEntity.ok(metadataMissingService.getIndex(
                new MetadataMissingAllReqRec(flowId, elementType, counterGroupType, counterGroupKey, timePeriod, counterKeys)));
    }

    @GetMapping(value = "/parseMap")
    public ResponseEntity<MetadataDefineParseMapReqRec> getParseMap(
            @RequestParam Long flowId,
            @RequestParam(required = false) String elementType,
            @RequestParam(required = false) String counterGroupType,
            @RequestParam String counterGroupKey,
            @RequestParam Long timePeriod,
            @RequestParam(required = false) List<String> counterKeys) {
        return ResponseEntity.ok(metadataMissingService.getParseMap(
                new MetadataMissingAllReqRec(flowId, elementType, counterGroupType, counterGroupKey, timePeriod, counterKeys)));
    }

    @GetMapping(value = "/parseTable")
    public ResponseEntity<MetadataDefineParseTableReqRec> getParseTable(
            @RequestParam Long flowId,
            @RequestParam(required = false) String elementType,
            @RequestParam(required = false) String counterGroupType,
            @RequestParam String counterGroupKey,
            @RequestParam Long timePeriod,
            @RequestParam(required = false) List<String> counterKeys) {
        return ResponseEntity.ok(metadataMissingService.getParseTable(
                new MetadataMissingAllReqRec(flowId, elementType, counterGroupType, counterGroupKey, timePeriod, counterKeys)));
    }

    @GetMapping(value = "/parseColumn")
    public ResponseEntity<MetadataDefineParseColumnsReqRec> getParseColumns(
            @RequestParam Long flowId,
            @RequestParam(required = false) String elementType,
            @RequestParam(required = false) String counterGroupType,
            @RequestParam String counterGroupKey,
            @RequestParam Long timePeriod,
            @RequestParam(required = false) List<String> counterKeys) {
        return ResponseEntity.ok(metadataMissingService.getParseColumn(
                new MetadataMissingAllReqRec(flowId, elementType, counterGroupType, counterGroupKey, timePeriod, counterKeys)));
    }

}
