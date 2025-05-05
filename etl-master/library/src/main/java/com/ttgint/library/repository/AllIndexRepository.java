package com.ttgint.library.repository;

import com.ttgint.library.model.AllIndex;
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
public interface AllIndexRepository extends JpaRepository<AllIndex, Long> {

    List<AllIndex> findAllByFlowIdAndIsActiveAndNeedRefreshAndIsGeneratedAndIsFailed(Long flowId,
                                                                                     Boolean isActive,
                                                                                     Boolean needRefresh,
                                                                                     Boolean isGenerated,
                                                                                     Boolean isFailed);

    @Modifying
    @Query("update AllIndex a " +
            "set a.updatedTime = :updatedTime, " +
            "a.needRefresh = :needRefresh, " +
            "a.isGenerated = :isGenerated, " +
            "a.isFailed = :isFailed " +
            "where a.id = :id ")
    void updateIndexStatusById(@Param("id") Long id,
                               @Param("updatedTime") OffsetDateTime updatedTime,
                               @Param("needRefresh") Boolean needRefresh,
                               @Param("isGenerated") Boolean isGenerated,
                               @Param("isFailed") Boolean isFailed);

    @Modifying
    @Query("update AllIndex a " +
            "set a.updatedTime = :updatedTime, " +
            "a.needRefresh = :needRefresh, " +
            "a.isGenerated = :isGenerated, " +
            "a.isFailed = :isFailed " +
            "where a.schemaName = :schemaName and a.tableName = :tableName " +
            "and a.isActive = true and a.needRefresh = true and a.isGenerated = false and a.isFailed = false")
    void updateIndexStatusBySchemaNameAndTableName(@Param("schemaName") String schemaName,
                                                   @Param("tableName") String tableName,
                                                   @Param("updatedTime") OffsetDateTime updatedTime,
                                                   @Param("needRefresh") Boolean needRefresh,
                                                   @Param("isGenerated") Boolean isGenerated,
                                                   @Param("isFailed") Boolean isFailed);

}
