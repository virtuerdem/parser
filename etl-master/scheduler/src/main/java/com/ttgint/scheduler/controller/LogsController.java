package com.ttgint.scheduler.controller;

import com.ttgint.scheduler.service.LogsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@RequestMapping("/logs")
@RequiredArgsConstructor
public class LogsController {

    private final LogsService logsService;

    @GetMapping("/list")
    public List<String> getLogs(
            @RequestParam String logDate,
            @RequestParam Long flowId,
            @RequestParam String flowProcessCode,
            @RequestParam String engineType,
            @RequestParam String logType) {
        return logsService.getLogFileContent(logDate, flowId, flowProcessCode, engineType, logType);
    }
}
