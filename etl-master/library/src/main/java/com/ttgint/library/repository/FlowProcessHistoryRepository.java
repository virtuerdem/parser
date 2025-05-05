package com.ttgint.library.repository;

import com.ttgint.library.model.FlowProcessHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface FlowProcessHistoryRepository extends JpaRepository<FlowProcessHistory, Long> {
    List<FlowProcessHistory> findAllByFlowId(Long flowId);
}