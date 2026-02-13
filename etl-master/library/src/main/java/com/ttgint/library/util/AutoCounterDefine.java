package com.ttgint.library.util;

import com.ttgint.library.model.AllCounter;
import com.ttgint.library.record.CounterDefineRecord;
import com.ttgint.library.record.ParseEngineRecord;
import com.ttgint.library.repository.AllCounterRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class AutoCounterDefine {

    protected final AllCounterRepository allCounterRepository;

    private final Set<CounterDefineRecord> records = ConcurrentHashMap.newKeySet();

    public AutoCounterDefine(AllCounterRepository allCounterRepository) {
        this.allCounterRepository = allCounterRepository;
    }

    public synchronized void collect(CounterDefineRecord record) {
        records.add(record);
    }

    public void collect(List<CounterDefineRecord> recordList) {
        records.addAll(recordList);
    }

    public Set<CounterDefineRecord> getAll() {
        return new HashSet<>(records);
    }

    public void clear() {
        records.clear();
    }

    public void save(ParseEngineRecord engineRecord) {
        List<AllCounter> savedCounters = allCounterRepository.findByFlowId(engineRecord.getFlowId());

        List<String> savedCounterKeys
                = savedCounters
                .stream()
                .map(e -> e.getElementType() + "|"
                        + e.getCounterGroupType() + "|"
                        + e.getCounterGroupKey() + "|"
                        + e.getCounterKey()).toList();
        log.info("* AutoCounterDefine savedCounterSize: {}", savedCounters.size());

        List<AllCounter> newCounters
                = getAll()
                .stream()
                .filter(e ->
                        !savedCounterKeys.contains(e.getElementType() + "|"
                                + e.getCounterGroupType() + "|"
                                + e.getCounterGroupKey() + "|"
                                + e.getCounterKey()))
                .map(e -> AllCounter.getRecord(engineRecord, e))
                .toList();
        log.info("* AutoCounterDefine newCounterSize: {}", newCounters.size());
        allCounterRepository.saveAll(newCounters);

        List<String> newCounterKeys
                = getAll()
                .stream()
                .map(e -> e.getElementType() + "|"
                        + e.getCounterGroupType() + "|"
                        + e.getCounterGroupKey() + "|"
                        + e.getCounterKey()).toList();

        List<AllCounter> oldCounters
                = savedCounters
                .stream()
                .filter(AllCounter::getIsActive)
                .filter(e ->
                        !newCounterKeys.contains(e.getElementType() + "|"
                                + e.getCounterGroupType() + "|"
                                + e.getCounterGroupKey() + "|"
                                + e.getCounterKey()))
                .peek(e -> e.setIsActive(false))
                .toList();
        log.info("* AutoCounterDefine oldCounterSize: {}", oldCounters.size());
        allCounterRepository.saveAll(oldCounters);
    }

}
