package com.ttgint.scheduler.cron;

import com.ttgint.library.repository.ManagerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeartbeatScheduler {

    @Value("${app.scheduler.managerCode}")
    private String managerCode;

    private final ManagerRepository managerRepository;

    @Transactional
    @Scheduled(cron = "*/10 * * * * *")
    public void updateHeartbeat() {
        managerRepository.updateHeartbeatTime(managerCode, OffsetDateTime.now());
    }

}
