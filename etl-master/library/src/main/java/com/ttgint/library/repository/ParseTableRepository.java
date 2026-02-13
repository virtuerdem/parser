package com.ttgint.library.repository;

import com.ttgint.library.model.ParseTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface ParseTableRepository extends JpaRepository<ParseTable, Long> {

    List<ParseTable> findAllByFlowIdAndIsActive(Long flowId, Boolean isActive);

    List<ParseTable> findAllByAllTableId(Long allTableId);

    List<ParseTable> findAllByAllTableIdAndObjectKey(Long allTableId, String objectKey);

}