package com.ttgint.library.repository;

import com.ttgint.library.model.ParseColumn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface ParseColumnRepository extends JpaRepository<ParseColumn, Long> {

    List<ParseColumn> findAllByFlowIdAndIsActive(Long flowId, Boolean isActive);


}