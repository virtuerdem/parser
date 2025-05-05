package com.ttgint;

import com.ttgint.library.enums.EngineType;
import com.ttgint.library.enums.FlowStatus;
import com.ttgint.library.enums.ManagerRole;
import com.ttgint.library.model.Flow;
import com.ttgint.library.model.FlowDetail;
import com.ttgint.library.model.Manager;
import com.ttgint.library.repository.FlowDetailRepository;
import com.ttgint.library.repository.FlowRepository;
import com.ttgint.library.repository.ManagerRepository;
import com.ttgint.scheduler.SchedulerApplication;
import com.ttgint.scheduler.cron.FlowDetailTimeoutScheduler;
import com.ttgint.scheduler.cron.FlowScheduler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@SpringBootTest(classes = SchedulerApplication.class)
public class DummyDataGenerateTest {

    @Value("${app.scheduler.managerCode}")
    private String managerCode;

    @Autowired
    private FlowRepository flowRepository;
    @Autowired
    private FlowDetailRepository flowDetailRepository;
    @Autowired
    private ManagerRepository managerRepository;
    @Autowired
    private FlowScheduler flowScheduler;
    @Autowired
    private FlowDetailTimeoutScheduler flowDetailTimeoutScheduler;

    @Test
    void generateMachineTest() {

        Manager m = new Manager();
        m.setManagerCode(UUID.randomUUID().toString());
        m.setManagerName("enbiya");
        m.setMachineId(1L);
        m.setPort(1111);
        m.setManagerRole(ManagerRole.SLAVE);
        m.setHeartbeatTime(OffsetDateTime.now());

        m.setIsActive(true);
        m.setCreatedTime(OffsetDateTime.now());
        m.setCreatedBy("system");
        managerRepository.save(m); //

        m.setId(null);
        m.setMachineId(2L);
        m.setManagerName("etl-ttg");
        m.setIsActive(true);
        managerRepository.save(m);
    }

    @Test
    void startFlowTest() {

        flowDetailTimeoutScheduler.killTimeoutProcesses();


//        long managerId = managerRepository.findByManagerCode(managerCode).getId();
//
//        flowRepository.findAllByIsActive(true)
//                .stream()
//                .filter(f -> f.getManagerId().equals(managerId))
//                .sorted(Comparator.comparing(Flow::getId))
//                .forEach(flow -> flowScheduler.executeFlow(flow));
    }

    @Test
    void generateFlowTest() {

        for (String measurement : List.of("CM", "PM")) {
            Flow flow = getFlow(measurement);
            flowRepository.save(flow);

            FlowDetail transferFlow = getFlowDetail(flow.getId(), EngineType.TRANSFER, 1);
            FlowDetail parseFlow = getFlowDetail(flow.getId(), EngineType.PARSE, 2);

            flowDetailRepository.save(transferFlow);
            flowDetailRepository.save(parseFlow);
        }

    }

    private Flow getFlow(String measurement) {
        return Flow.builder()
                .flowCode(String.format("VF_TR_D_HW_ENB_%s", measurement)) // VF_TR_D_HW_ENB_PM
                .flowCron("* * * * * * | 5 * * * * *")
                .flowStatus(FlowStatus.STOPPED)
                .flowInfo(null)
                .isActive(true)
                .createdTime(OffsetDateTime.now())
                .createdBy("system")
                .updatedTime(OffsetDateTime.now())
                .updatedBy("system")
                .extraInfo("for test..")
                .build();
    }

    private FlowDetail getFlowDetail(Long flowId, EngineType engineType, Integer order) {
        return FlowDetail.builder()
                .flowId(flowId)
                .engineType(engineType)
                .executeOrder(order)
                .engineStatus(FlowStatus.STOPPED)
                .isActive(Boolean.TRUE)
                .createdTime(OffsetDateTime.now())
                .createdBy("system")
                .updatedTime(OffsetDateTime.now())
                .updatedBy("system")
                .build();
    }

}
