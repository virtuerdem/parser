package com.ttgint.library.repository;

import com.ttgint.library.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Repository 
public interface BranchRepository extends JpaRepository<Branch, Long> {

    List<Branch> findAllByIsActive(Boolean isActive);

}