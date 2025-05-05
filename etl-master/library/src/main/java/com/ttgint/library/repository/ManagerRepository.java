package com.ttgint.library.repository;

import com.ttgint.library.model.Manager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Transactional
@Repository
public interface ManagerRepository extends JpaRepository<Manager, Long> {

    Manager findByManagerCode(String managerCode);

    @Modifying
    @Query("update Manager m set m.heartbeatTime = :heartbeatTime where m.managerCode = :managerCode")
    void updateHeartbeatTime(@Param(value = "managerCode") String managerCode,
                             @Param(value = "heartbeatTime") OffsetDateTime heartbeatTime);

    @Modifying
    @Query("update Manager m set m.managerRole = 'SLAVE'")
    void updateAllAsSlave();

}