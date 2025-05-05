package com.ttgint.library.repository;

import com.ttgint.library.model.Connection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface ConnectionRepository extends JpaRepository<Connection, Long> {

    List<Connection> findByFlowIdAndIsActive(Long id, Boolean isActive);

}