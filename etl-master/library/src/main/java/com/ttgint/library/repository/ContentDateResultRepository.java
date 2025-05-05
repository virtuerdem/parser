package com.ttgint.library.repository;

import com.ttgint.library.model.ContentDateResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public interface ContentDateResultRepository extends JpaRepository<ContentDateResult, Long> {
}
