package com.ttgint.scheduler.cron;

import com.ttgint.library.enums.FlowStatus;
import com.ttgint.library.enums.ManagerRole;
import com.ttgint.library.model.Flow;
import com.ttgint.library.model.Manager;
import com.ttgint.library.repository.FlowRepository;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class FlowFailoverScheduler {

    @Value("${app.scheduler.managerCode}")
    private String managerCode;

    @Value("${app.scheduler.masterCheckInterval}")
    private long masterCheckInterval; // Master kontrol süresi (saniye)

    private final ManagerRepository managerRepository;
    private final FlowRepository flowRepository;

    @Scheduled(cron = "*/10 * * * * *")
    @Transactional
    public void checkFlowBalance() {
        checkFailover();
        checkFailback();
    }

    private void checkFailover() {
        Manager manager = getCurrentManager();
        if (manager == null || !ManagerRole.MASTER.equals(manager.getManagerRole())) return;

        OffsetDateTime threshold = OffsetDateTime.now().minusSeconds(masterCheckInterval * 2);
        List<Manager> allManagers = managerRepository.findAll();

        List<Long> aliveManagerIds = getAliveManagerIds(allManagers, threshold);
        List<Long> deadManagerIds = getDeadManagerIds(allManagers, threshold);

        if (deadManagerIds.isEmpty() || aliveManagerIds.isEmpty()) return;

        List<Flow> downFlows = flowRepository.findAll().stream()
                .filter(f -> f.getIsActive() && f.getIsMovable() && deadManagerIds.contains(f.getActiveManagerId()))
                .toList();

        if (downFlows.isEmpty()) return;

        distributeFlows(downFlows, aliveManagerIds);
    }

    private void checkFailback() {
        Manager manager = getCurrentManager();
        if (manager == null || !Boolean.TRUE.equals(manager.getIsActive())) return;

        List<Flow> flowsToReassign = flowRepository.findAll().stream()
                .filter(f -> f.getIsActive() && manager.getId().equals(f.getRootManagerId()))
                .toList();

        if (flowsToReassign.isEmpty()) return;

        for (Flow flow : flowsToReassign) {
            flow.setActiveManagerId(manager.getId());
            flow.setUpdatedTime(OffsetDateTime.now());
            flow.setUpdatedBy(FlowFailoverScheduler.class.getSimpleName());
            flowRepository.save(flow);
        }
    }

    private Manager getCurrentManager() {
        Manager manager = managerRepository.findByManagerCode(managerCode);
        if (manager == null) {
            log.warn("Manager with code {} not found.", managerCode);
        }
        return manager;
    }

    private List<Long> getAliveManagerIds(List<Manager> allManagers, OffsetDateTime threshold) {
        return allManagers.stream()
                .filter(m -> m.getHeartbeatTime() != null && m.getHeartbeatTime().isAfter(threshold))
                .filter(m -> Boolean.TRUE.equals(m.getIsActive()))
                .sorted(Comparator.comparing(Manager::getId))
                .map(Manager::getId)
                .toList();
    }

    private List<Long> getDeadManagerIds(List<Manager> allManagers, OffsetDateTime threshold) {
        return allManagers.stream()
                .filter(m -> m.getHeartbeatTime() == null || m.getHeartbeatTime().isBefore(threshold))
                .map(Manager::getId)
                .toList();
    }

    private void distributeFlows(List<Flow> flows, List<Long> aliveManagerIds) {
        for (int i = 0; i < flows.size(); i++) {
            Flow flow = flows.get(i);
            Long targetManagerId = aliveManagerIds.get(i % aliveManagerIds.size());
            flow.setActiveManagerId(targetManagerId);
            flow.setUpdatedTime(OffsetDateTime.now());
            flow.setUpdatedBy(FlowFailoverScheduler.class.getSimpleName());
            flow.setFlowStatus(FlowStatus.STOPPED);
            flowRepository.save(flow);

            NotificationUtils.create("0002", String.format("Flow moved to another manager! Root Manager: %s Active Manager: %s", flow.getRootManagerId(), flow.getActiveManagerId()));
        }
    }

}
