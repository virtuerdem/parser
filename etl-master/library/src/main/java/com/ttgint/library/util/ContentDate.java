package com.ttgint.library.util;

import com.ttgint.library.record.ContentDateResultRecord;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ContentDate {

    private final Set<ContentDateResultRecord> records = ConcurrentHashMap.newKeySet();

    public void collect(ContentDateResultRecord record) {
        records.add(record);
    }

    public void collect(List<ContentDateResultRecord> recordList) {
        records.addAll(recordList);
    }

    public Set<ContentDateResultRecord> getAll() {
        return new HashSet<>(records);
    }

    public List<ContentDateResultRecord> getContentDateByFileName(String fileName) {
        return records.stream()
                .filter(e -> e.getFileName().equals(fileName))
                .toList();
    }

    public List<OffsetDateTime> getDates() {
        return records.stream()
                .map(ContentDateResultRecord::getFragmentDate)
                .distinct()
                .sorted()
                .toList();
    }

    public void printDates() {
        List<OffsetDateTime> dateTimeStream = getDates();
        log.info("* ContentDate discovered dateSize: {}", dateTimeStream.size());
        dateTimeStream.forEach(e -> log.info("> {}",
                e.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX")))
        );
    }

    public void clear() {
        records.clear();
    }

}
