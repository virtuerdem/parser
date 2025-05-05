package com.ttgint.library.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ProgressType {

    TEST("TEST"),
    PRODUCT("PRODUCT");

    @JsonValue
    private final String value;

}
