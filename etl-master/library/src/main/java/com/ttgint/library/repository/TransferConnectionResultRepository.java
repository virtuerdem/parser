package com.ttgint.library.repository;

import com.ttgint.library.model.TransferConnectionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Transactional
@Repository
public interface TransferConnectionResultRepository extends JpaRepository<TransferConnectionResult, Long> {

    @Query("SELECT MAX(a.fragmentTime) " +
            "FROM TransferConnectionResult a " +
            "WHERE a.connectionId = :connectionId ")
    OffsetDateTime getMaxModifiedTime(@Param("connectionId") Long connectionId);

    @Query("FROM TransferConnectionResult a "
            + "WHERE a.connectionId = :connectionId "
            + "and a.isDownloaded = false "
            + "and a.transferTryCount >= :minTryCount "
            + "and a.transferTryCount < :maxTryCount "
            + "and a.fragmentTime >= :minFragmentTime "
            + "and a.fragmentTime < :maxFragmentTime ")
    List<TransferConnectionResult> getFileListToTransfer(@Param("connectionId") Long connectionId,
                                                         @Param("minTryCount") Integer minTryCount,
                                                         @Param("maxTryCount") Integer maxTryCount,
                                                         @Param("minFragmentTime") OffsetDateTime minFragmentTime,
                                                         @Param("maxFragmentTime") OffsetDateTime maxFragmentTime);

}