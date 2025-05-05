package com.ttgint.library.repository;

import com.ttgint.library.model.AllTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Transactional
@Repository
public interface AllTableRepository extends JpaRepository<AllTable, Long> {

    List<AllTable> findAllByFlowIdAndIsActiveAndNeedRefreshAndIsGeneratedAndIsFailed(Long flowId,
                                                                                     Boolean isActive,
                                                                                     Boolean needRefresh,
                                                                                     Boolean isGenerated,
                                                                                     Boolean isFailed);

    @Modifying
    @Query("update AllTable a " +
            "set a.updatedTime = :updatedTime, " +
            "a.needRefresh = :needRefresh, " +
            "a.isGenerated = :isGenerated, " +
            "a.isFailed = :isFailed " +
            "where a.id = :id ")
    void updateTableStatusById(@Param("id") Long id,
                               @Param("updatedTime") OffsetDateTime updatedTime,
                               @Param("needRefresh") Boolean needRefresh,
                               @Param("isGenerated") Boolean isGenerated,
                               @Param("isFailed") Boolean isFailed);

}
