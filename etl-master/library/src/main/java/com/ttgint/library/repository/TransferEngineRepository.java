package com.ttgint.library.repository;

import com.ttgint.library.model.TransferEngine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
@Repository
public interface TransferEngineRepository extends JpaRepository<TransferEngine, Long> {

    Optional<TransferEngine> findByFlowId(Long flowId);

}