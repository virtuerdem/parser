package com.ttgint.library.util;

import com.ttgint.library.model.ContentDateResult;
import com.ttgint.library.record.ContentDateReaderRecord;
import com.ttgint.library.record.ContentDateResultRecord;
import com.ttgint.library.repository.ContentDateResultRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.io.BufferedReader;
import java.io.FileReader;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class ContentDateReader implements Runnable {

    private final ContentDateReaderRecord record;
    private final ContentDateResultRepository contentDateResultRepository;
    private final ContentDate contentDate;

    public ContentDateReader(ApplicationContext applicationContext, ContentDateReaderRecord contentDateReaderRecord) {
        this.record = contentDateReaderRecord;
        this.contentDateResultRepository = applicationContext.getBean(ContentDateResultRepository.class);
        this.contentDate = applicationContext.getBean(ContentDate.class);
    }

    @Override
    public void run() {
        find();
    }

    private void find() {
        HashMap<String, Long> fileContentDateMap = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(record.getFile()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String dateValue
                        = line.split("\\" + record.getResultFileDelimiter())[record.getDateColumnIndex() - 1];
                if (!fileContentDateMap.containsKey(dateValue)) {
                    fileContentDateMap.put(dateValue, 1L);
                } else {
                    fileContentDateMap.put(dateValue, fileContentDateMap.get(dateValue) + 1L);
                }
            }
        } catch (Exception exception) {
            log.error("* FindDataDates read fileName: {}", record.getFile().getName(), exception);
        }
        insertResults(fileContentDateMap);
    }

    private void insertResults(HashMap<String, Long> fileContentDateMap) {
        List<ContentDateResultRecord> records
                = fileContentDateMap.keySet().stream()
                .map(e -> ContentDateResultRecord
                        .recordToEntity(record, getContentDate(e), fileContentDateMap.get(e)))
                .toList();

        contentDate.collect(records);

        if (record.getNeedResult()) {
            contentDateResultRepository.saveAll(records.stream().map(ContentDateResult::recordToEntity).toList());
        }
    }

    private OffsetDateTime getContentDate(String dateValue) {
        return OffsetDateTime.parse(dateValue,
                DateTimeFormatter.ofPattern(
                        dbDateFormatToJavaDateFormat(record.getDbDateFormat())));
    }

    private String dbDateFormatToJavaDateFormat(String dateFormat) {
        return dateFormat.toUpperCase()
                .replace("YYYY", "yyyy")
                .replace("DD", "dd")
                .replace("HH12", "hh")
                .replace("HH24", "HH")
                .replace("MI", "mm")
                .replace("SS", "ss");
    }

    private String javaDateFormatToDbDateFormat(String dateFormat) {
        return dateFormat.toLowerCase()
                .replace("yyyy", "YYYY")
                .replace("dd", "DD")
                .replace("HH", "HH24")
                .replace("mm", "MI")
                .replace("ss", "SS")
                .replace("z", "Z");
    }
}
