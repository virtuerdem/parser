package com.ttgint.library.repository;

import com.ttgint.library.model.TransferConnectionHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public interface TransferConnectionHistoryRepository extends JpaRepository<TransferConnectionHistory, Long> {
}