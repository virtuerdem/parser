package com.ttgint.library.repository;

import com.ttgint.library.model.LoaderResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Optional;

@Transactional
@Repository
public interface LoaderResultRepository extends JpaRepository<LoaderResult, Long> {

    Optional<LoaderResult> findByFlowIdAndSchemaNameAndTableNameAndFragmentDate(Long flowId,
                                                                                String schemaName,
                                                                                String tableName,
                                                                                OffsetDateTime fragmentDate);
}
