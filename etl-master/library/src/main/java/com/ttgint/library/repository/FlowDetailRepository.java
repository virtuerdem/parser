package com.ttgint.library.repository;

import com.ttgint.library.enums.FlowStatus;
import com.ttgint.library.model.FlowDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface FlowDetailRepository extends JpaRepository<FlowDetail, Long> {

    List<FlowDetail> findByFlowId(Long flowId);

    List<FlowDetail> findAllByFlowIdAndEngineStatus(Long flowId, FlowStatus engineStatus);

    List<FlowDetail> findAllByEngineStatusIn(List<FlowStatus> statusList);

    @Modifying
    @Query("update FlowDetail f set f.engineStatus = :newEngineStatus " +
            "where f.flowId = :flowId and f.engineStatus = :engineStatus")
    void updateStatusByFlowId(@Param("flowId") Long flowId,
                              @Param("engineStatus") FlowStatus engineStatus,
                              @Param("newEngineStatus") FlowStatus newEngineStatus);

    @Modifying
    @Query("update FlowDetail f set f.engineStatus = :newEngineStatus " +
            "where f.id = :id")
    void updateStatusById(@Param("id") Long id,
                          @Param("newEngineStatus") FlowStatus newEngineStatus);

}