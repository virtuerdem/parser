package com.ttgint.library.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PathWalkMethod {

    CURRENT("CURRENT"),
    NESTED("NESTED");

    @JsonValue
    private final String value;

}
