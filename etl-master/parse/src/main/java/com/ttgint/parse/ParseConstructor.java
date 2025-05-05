package com.ttgint.parse;

import com.ttgint.library.model.Branch;
import com.ttgint.library.model.Flow;
import com.ttgint.library.model.ParseComponent;
import com.ttgint.library.model.ParseEngine;
import com.ttgint.library.record.EngineArgumentRecord;
import com.ttgint.library.record.ParseEngineRecord;
import com.ttgint.library.repository.BranchRepository;
import com.ttgint.library.repository.FlowRepository;
import com.ttgint.library.repository.ParseComponentRepository;
import com.ttgint.library.repository.ParseEngineRepository;
import com.ttgint.parse.base.ParseBaseEngine;
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
public class ParseConstructor {

    private final ApplicationContext applicationContext;
    private final ApplicationArguments args;
    private final FlowRepository flowRepository;
    private final BranchRepository branchRepository;
    private final ParseEngineRepository parseEngineRepository;
    private final ParseComponentRepository parseComponentRepository;

    @PostConstruct
    public void start() {
        log.info("* ParseConstructor has started, PID: {}", ProcessHandle.current().pid());

        EngineArgumentRecord argument
                = EngineArgumentRecord.getRecord(args);
        if (argument.getFlowId() == null || argument.getFlowProcessCode() == null) {
            log.error("! ApplicationArguments is not defined");
            return;
        }

        Optional<ParseEngine> engine
                = parseEngineRepository.findByFlowId(argument.getFlowId());
        if (engine.isEmpty()) {
            log.error("! ParseConstructor is not defined");
            return;
        } else if (!engine.get().getIsActive()) {
            log.error("! ParseConstructor is not active");
            return;
        }

        Optional<ParseComponent> component
                = parseComponentRepository.findById(engine.get().getParseComponentId());
        if (component.isEmpty()) {
            log.error("! ParseComponent is not defined");
            return;
        } else if (!component.get().getIsActive()) {
            log.error("! ParseComponent is not active");
            return;
        }

        try {
            Flow flow = flowRepository.findById(argument.getFlowId()).get();
            Branch branch = branchRepository.findById(flow.getBranchId()).get();

            ParseBaseEngine bean = (ParseBaseEngine) applicationContext
                    .getBean(component.get()
                            .getComponentCode()
                            .toUpperCase().trim());
            bean.startEngine(ParseEngineRecord.getRecord(branch, flow, argument.getFlowProcessCode(), engine.get()));
        } catch (Exception executeException) {
            log.error("! ParseEngine has executeException ", executeException);
        }

        log.info("* ParseConstructor has done");
    }

}
