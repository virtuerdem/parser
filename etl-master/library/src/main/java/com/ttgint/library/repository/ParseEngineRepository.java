package com.ttgint.library.repository;

import com.ttgint.library.model.ParseEngine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Repository
public interface ParseEngineRepository extends JpaRepository<ParseEngine, Long> {

    Optional<ParseEngine> findByFlowId(Long flowId);

}