package com.ttgint.library.repository;

import com.ttgint.library.model.TransferComponent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public interface TransferComponentRepository extends JpaRepository<TransferComponent, Long> {
}
