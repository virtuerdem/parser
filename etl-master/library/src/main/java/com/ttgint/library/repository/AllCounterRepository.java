package com.ttgint.library.repository;

import com.ttgint.library.model.AllCounter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository
public interface AllCounterRepository extends JpaRepository<AllCounter, Long> {

    List<AllCounter> findByFlowId(Long flowId);

}
