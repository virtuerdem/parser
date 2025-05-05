package com.ttgint.transfer;

import com.ttgint.library.model.Branch;
import com.ttgint.library.model.Flow;
import com.ttgint.library.model.TransferComponent;
import com.ttgint.library.model.TransferEngine;
import com.ttgint.library.record.EngineArgumentRecord;
import com.ttgint.library.record.TransferEngineRecord;
import com.ttgint.library.repository.BranchRepository;
import com.ttgint.library.repository.FlowRepository;
import com.ttgint.library.repository.TransferComponentRepository;
import com.ttgint.library.repository.TransferEngineRepository;
import com.ttgint.transfer.base.TransferBaseEngine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferConstructor {

    private final ApplicationContext applicationContext;
    private final ApplicationArguments args;
    private final FlowRepository flowRepository;
    private final BranchRepository branchRepository;
    private final TransferEngineRepository transferEngineRepository;
    private final TransferComponentRepository transferComponentRepository;

    @PostConstruct
    public void start() {
        log.info("* TransferConstructor has started, PID: {}", ProcessHandle.current().pid());

        EngineArgumentRecord argument
                = EngineArgumentRecord.getRecord(args);
        if (argument.getFlowId() == null || argument.getFlowProcessCode() == null) {
            log.error("! ApplicationArguments is not defined");
            return;
        }

        Optional<TransferEngine> engine
                = transferEngineRepository.findByFlowId(argument.getFlowId());
        if (engine.isEmpty()) {
            log.error("! TransferConstructor is not defined");
            return;
        } else if (!engine.get().getIsActive()) {
            log.error("! TransferConstructor is not active");
            return;
        }

        Optional<TransferComponent> component
                = transferComponentRepository.findById(engine.get().getTransferComponentId());
        if (component.isEmpty()) {
            log.error("! TransferComponent is not defined");
            return;
        } else if (!component.get().getIsActive()) {
            log.error("! TransferComponent is not active");
            return;
        }

        try {
            Flow flow = flowRepository.findById(argument.getFlowId()).get();
            Branch branch = branchRepository.findById(flow.getBranchId()).get();

            TransferBaseEngine bean = (TransferBaseEngine) applicationContext
                    .getBean(component.get()
                            .getComponentCode()
                            .toUpperCase().trim());
            bean.startEngine(TransferEngineRecord.getRecord(branch, flow, argument.getFlowProcessCode(), engine.get()));
        } catch (Exception executeException) {
            log.error("! TransferEngine has executeException ", executeException);
        }

        log.info("* TransferConstructor has done");
    }

}
