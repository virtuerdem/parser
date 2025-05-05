package com.ttgint.library.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FileInfo {

    FILE("FILE"),
    FOLDER("FOLDER");

    @JsonValue
    private final String value;

}
