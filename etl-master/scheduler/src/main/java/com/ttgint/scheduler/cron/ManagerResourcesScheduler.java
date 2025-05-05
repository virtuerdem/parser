package com.ttgint.scheduler.cron;

import com.ttgint.library.model.Manager;
import com.ttgint.library.model.ManagerMonitoring;
import com.ttgint.library.repository.ManagerMonitoringRepository;
import com.ttgint.library.repository.ManagerRepository;
import com.ttgint.scheduler.utils.SystemResourceUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class ManagerResourcesScheduler {

    private final ManagerRepository managerRepository;
    private final ManagerMonitoringRepository managerMonitoringRepository;
    private final SystemResourceUtils systemResourceUtils;

    @Value("${app.scheduler.managerCode}")
    private String managerCode;

    @Scheduled(cron = "*/30 * * * * *")
    public void updateResources() {
        Manager manager = managerRepository.findByManagerCode(managerCode);

        if (manager == null) throw new RuntimeException("Manager not found with code: " + managerCode);

        if (!manager.getIsActive()) return;

        double[] loadAverages = systemResourceUtils.getLoadAverages();
        manager.setLoadAverage1(loadAverages[0]);
        manager.setLoadAverage5(loadAverages[1]);
        manager.setLoadAverage15(loadAverages[2]);
        manager.setUptime(systemResourceUtils.getUptime());
        manager.setCpuUsage(systemResourceUtils.getCpuUsage());
        manager.setMemoryUsage(systemResourceUtils.getMemoryUsage());
        manager.setSwapUsage(systemResourceUtils.getSwapUsage());
        manager.setDiskUsage(systemResourceUtils.getDiskUsage());
        manager.setCheckTime(OffsetDateTime.now());
        manager.setUpdatedTime(OffsetDateTime.now());
        manager.setUpdatedBy(this.getClass().getSimpleName());

        managerRepository.save(manager);

        if (manager.getIsActiveMonitoring()) {
            addResourceHistory(manager); // history
        }
    }

    private void addResourceHistory(Manager manager) {

        ManagerMonitoring m = ManagerMonitoring.builder()
                .managerId(manager.getId())
                .checkTime(manager.getCheckTime() != null ? manager.getCheckTime() : OffsetDateTime.now())
                .uptime(manager.getUptime())
                .loadAverage(manager.getLoadAverage15())
                .loadAverage1(manager.getLoadAverage1())
                .loadAverage5(manager.getLoadAverage5())
                .loadAverage15(manager.getLoadAverage15())
                .cpuUsage(manager.getCpuUsage())
                .memoryUsage(manager.getMemoryUsage())
                .swapUsage(manager.getSwapUsage())
                .diskUsage(manager.getDiskUsage())
                .build();

        managerMonitoringRepository.save(m);
    }

}
