package com.ttgint.library.repository;

import com.ttgint.library.model.AggregateJobs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public interface AggregateJobsRepository extends JpaRepository<AggregateJobs, Long> {
}
