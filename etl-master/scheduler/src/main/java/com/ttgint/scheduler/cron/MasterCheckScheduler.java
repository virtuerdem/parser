package com.ttgint.scheduler.cron;

import com.ttgint.library.enums.ManagerRole;
import com.ttgint.library.model.Manager;
import com.ttgint.library.repository.ManagerRepository;
import com.ttgint.library.util.NotificationUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MasterCheckScheduler {

    @Value("${app.scheduler.managerCode}")
    private String managerCode;

    @Value("${app.scheduler.masterCheckInterval}")
    private long masterCheckInterval; // Master kontrol süresi (saniye)

    private final ManagerRepository managerRepository;

    @Scheduled(cron = "*/10 * * * * *")
    @Transactional
    public void checkMaster() {
        OffsetDateTime threshold = OffsetDateTime.now().minusSeconds(masterCheckInterval * 2);
        List<Manager> managers = managerRepository.findAll().stream().filter(m -> m.getHeartbeatTime() != null).toList();
        Optional<Manager> currentMaster = managers.stream()
                .filter(m -> m.getIsActive() && m.getHeartbeatTime().isAfter(threshold))
                .min(Comparator.comparing(Manager::getId));

        if (currentMaster.isEmpty()
                || !currentMaster.get().getIsActive()
                || currentMaster.get().getManagerRole().equals(ManagerRole.SLAVE)) {
            log.info("Master down! New master will be selected...");
            selectNewMaster(managers);
        }
    }

    private void selectNewMaster(List<Manager> managers) {
        OffsetDateTime threshold = OffsetDateTime.now().minusSeconds(masterCheckInterval * 2);
        Optional<Manager> newMaster = managers.stream()
                .filter(m -> m.getHeartbeatTime().isAfter(threshold))
                .min(Comparator.comparing(Manager::getId));

        newMaster.ifPresent(m -> {
            if (m.getManagerCode().equals(managerCode)) {

                managerRepository.updateAllAsSlave(); // down olan master -> slave
                m.setManagerRole(ManagerRole.MASTER);
                managerRepository.save(m);

                NotificationUtils.create("0001", String.format("Manager: '%s' has been selected as a MASTER!", m.getManagerName()));
                log.info("Manager: '{}' has been selected as a MASTER!", m.getManagerName());
            }
        });
    }
}
