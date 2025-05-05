package com.ttgint.library.repository;

import com.ttgint.library.model.NetworkNode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface NetworkNodeRepository extends JpaRepository<NetworkNode, Long> {

    @Query("SELECT a.nodeName "
            + "FROM NetworkNode a "
            + "WHERE a.flowId = :flowId "
            + "and a.isActive = true ")
    List<String> findActiveNodeNamesByFlowId(@Param("flowId") Long flowId);

    List<NetworkNode> findByFlowIdAndIsActive(Long flowId, Boolean isActive);

    @Query("SELECT a.nodeName " + "FROM NetworkNode a " + "WHERE a.branchId = :branchId " + "and a.isActive = true ")
    List<String> findActiveNodeNamesByBranchId(@Param("branchId") Long branchId);

    List<NetworkNode> findByBranchIdAndIsActive(Long branchId, Boolean isActive);

}
