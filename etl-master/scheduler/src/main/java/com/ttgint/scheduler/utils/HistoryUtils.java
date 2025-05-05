package com.ttgint.scheduler.utils;

import com.ttgint.library.model.FlowProcessHistory;
import com.ttgint.library.repository.FlowProcessHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
@RequiredArgsConstructor
public class HistoryUtils {

    private final FlowProcessHistoryRepository flowHistoryRepository;

    public FlowProcessHistory startFlowHistory(Long flowId, String flowProcessCode) {
        FlowProcessHistory history = FlowProcessHistory.builder()
                .flowId(flowId)
                .flowProcessCode(flowProcessCode)
                .flowStartTime(OffsetDateTime.now())
                .build();

        flowHistoryRepository.save(history);
        return history;
    }

    public FlowProcessHistory endFlowHistory(FlowProcessHistory history) {
        history.setFlowEndTime(OffsetDateTime.now());
        flowHistoryRepository.save(history);
        return history;
    }

}
