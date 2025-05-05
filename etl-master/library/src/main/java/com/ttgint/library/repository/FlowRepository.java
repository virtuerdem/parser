package com.ttgint.library.repository;

import com.ttgint.library.enums.FlowStatus;
import com.ttgint.library.model.Flow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface FlowRepository extends JpaRepository<Flow, Long> {

    List<Flow> findAllByIsActive(Boolean isActive);

    @Modifying
    @Query("update Flow f set f.flowStatus = :flowStatus where f.id = :id")
    void updateFlowStatusById(@Param("id") Long id, @Param("flowStatus") FlowStatus flowStatus);

}