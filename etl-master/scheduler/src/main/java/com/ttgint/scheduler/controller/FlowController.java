package com.ttgint.scheduler.controller;

import com.ttgint.library.model.Flow;
import com.ttgint.library.model.FlowDetail;
import com.ttgint.library.model.FlowProcessHistory;
import com.ttgint.scheduler.service.FlowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/flows")
@RequiredArgsConstructor
public class FlowController {

    private final FlowService flowService;

    @GetMapping("/list")
    public ResponseEntity<List<Flow>> getAllFlows() {
        return ResponseEntity.ok(flowService.getAllFlows());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Flow> getFlowById(@PathVariable Long id) {
        return ResponseEntity.ok(flowService.getFlowById(id));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<List<FlowDetail>> getFlowDetailsByFlowId(@PathVariable Long id) {
        return ResponseEntity.ok(flowService.getFlowDetailsByFlowId(id));
    }

    @GetMapping("/{id}/process-history")
    public ResponseEntity<List<FlowProcessHistory>> getFlowProcessHistoryByFlowId(@PathVariable Long id) {
        return ResponseEntity.ok(flowService.getFlowProcessHistoryByFlowId(id));
    }

}