package com.ttgint.library.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    UNKNOWN("UNKNOWN"),
    ALARM("ALARM"),
    INFO("INFO"),
    WARNING("WARNING"),
    CRITICAL("CRITICAL");

    @JsonValue
    private final String value;
}
