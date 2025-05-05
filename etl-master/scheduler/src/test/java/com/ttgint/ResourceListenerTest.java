package com.ttgint;

import com.ttgint.scheduler.SchedulerApplication;
import com.ttgint.scheduler.cron.ManagerResourcesScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = SchedulerApplication.class)
public class ResourceListenerTest {

    @Autowired
    private ManagerResourcesScheduler scheduler;

    @Test
    void generateMachineTest() {
        scheduler.updateResources();
    }

}
