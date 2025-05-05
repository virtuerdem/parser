package com.ttgint.scheduler.service;

import com.ttgint.library.model.Flow;
import com.ttgint.library.model.FlowDetail;
import com.ttgint.library.model.FlowProcessHistory;
import com.ttgint.library.repository.FlowDetailRepository;
import com.ttgint.library.repository.FlowProcessHistoryRepository;
import com.ttgint.library.repository.FlowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FlowService {

    private final FlowRepository flowRepository;
    private final FlowDetailRepository flowDetailRepository;
    private final FlowProcessHistoryRepository flowProcessHistoryRepository;

    public List<Flow> getAllFlows() {
        return flowRepository.findAll();
    }

    public Flow getFlowById(Long id) {
        return flowRepository.findById(id).orElseThrow(() -> new RuntimeException("Flow not found"));
    }

    public List<FlowDetail> getFlowDetailsByFlowId(Long id) {
        return flowDetailRepository.findByFlowId(id);
    }

    public List<FlowProcessHistory> getFlowProcessHistoryByFlowId(Long id) {
        return flowProcessHistoryRepository.findAllByFlowId(id);
    }

}
