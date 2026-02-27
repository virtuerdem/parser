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

    List<NetworkItem> findByBranchIdAndIsActive(Long branchId, Boolean isActive);

    List<NetworkItem> findByFlowIdAndIsActive(Long flowId, Boolean isActive);

    @Query("SELECT a.itemCode "
            + "FROM NetworkItem a "
            + "WHERE a.branchId = :branchId "
            + "and a.isActive = true ")
    List<String> findActiveItemCodesByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT a.itemCode "
            + "FROM NetworkItem a "
            + "WHERE a.flowId = :flowId "
            + "and a.isActive = true ")
    List<String> findActiveItemCodesByFlowId(@Param("flowId") Long flowId);

    @Query("SELECT a.itemName "
            + "FROM NetworkItem a "
            + "WHERE a.branchId = :branchId "
            + "and a.isActive = true ")
    List<String> findActiveItemNamesByBranchId(@Param("branchId") Long branchId);

    @Query("SELECT a.itemName "
            + "FROM NetworkItem a "
            + "WHERE a.flowId = :flowId "
            + "and a.isActive = true ")
    List<String> findActiveItemNamesByFlowId(@Param("flowId") Long flowId);

}
