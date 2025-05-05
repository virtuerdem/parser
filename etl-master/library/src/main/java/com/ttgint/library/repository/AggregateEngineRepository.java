package com.ttgint.library.repository;

import com.ttgint.library.model.AggregateEngine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public interface AggregateEngineRepository extends JpaRepository<AggregateEngine, Long> {
}