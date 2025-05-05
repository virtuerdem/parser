package com.ttgint.library.repository;

import com.ttgint.library.model.NetworkItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface NetworkItemRepository extends JpaRepository<NetworkItem, Long> {

    @Query("SELECT a.itemCode "
            + "FROM NetworkItem a "
            + "WHERE a.flowId = :flowId "
            + "and a.isActive = true ")
    List<String> findActiveItemCodesByFlowId(@Param("flowId") Long flowId);

    List<NetworkItem> findByFlowIdAndIsActive(Long flowId, Boolean isActive);

}
