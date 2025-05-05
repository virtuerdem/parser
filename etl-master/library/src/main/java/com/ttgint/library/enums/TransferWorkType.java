package com.ttgint.library.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TransferWorkType {

    MODIFIED("MODIFIED"),
    BACKLOG("BACKLOG"),
    ALL("ALL");

    @JsonValue
    private final String value;
}
