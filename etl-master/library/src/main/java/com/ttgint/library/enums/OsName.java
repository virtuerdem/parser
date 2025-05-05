package com.ttgint.library.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OsName {

    LINUX("LINUX"),
    WINDOWS("WINDOWS"),
    MACOS("MACOS");

    @JsonValue
    private final String value;

}
