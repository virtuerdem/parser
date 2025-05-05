package com.ttgint.library.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FlowStatus {

    STOPPED("STOPPED"),
    RUNNING("RUNNING"),
    TRIGGERED("TRIGGERED"),
    FORCED("FORCED");

    @JsonValue
    private final String value;

}
