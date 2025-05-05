package com.ttgint.library.repository;

import com.ttgint.library.model.AppEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public interface AppEnvironmentRepository extends JpaRepository<AppEnvironment, Long> {
}
