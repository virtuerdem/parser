package com.ttgint.scheduler.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class LogsService {

    @Value("${app.scheduler.logPath}")
    private String logPath;

    public List<String> getLogFileContent(String logDate,
                                          Long flowId,
                                          String flowProcessCode,
                                          String engineType,
                                          String logType) {

        OffsetDateTime date = OffsetDateTime.parse(logDate, DateTimeFormatter.ISO_LOCAL_DATE);
        String logFileName = String.format("%s/%s/%s/%s/%s/%s_%s_%s.log",
                logPath,
                date.getYear(),
                String.format("%02d", date.getMonthValue()),
                String.format("%02d", date.getDayOfMonth()),
                flowId,
                flowProcessCode,
                engineType,
                logType).replace("//", "/");

        Path path = Paths.get(logFileName);
        if (!Files.exists(path)) {
            throw new RuntimeException("Log file not found: " + logFileName);
        }

        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            throw new RuntimeException("Error reading log file: " + logFileName, e);
        }

    }

}
