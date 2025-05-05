package com.ttgint.library.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EngineType {

    AGGREGATE("AGGREGATE"),
    ARCHIVE("ARCHIVE"),
    EXPORT("EXPORT"),
    LOADER("LOADER"),
    NODIUS("NODIUS"),
    PARSE("PARSE"),
    TRANSFER("TRANSFER"),
    VALIDATION("VALIDATION");

    @JsonValue
    private final String value;
}
