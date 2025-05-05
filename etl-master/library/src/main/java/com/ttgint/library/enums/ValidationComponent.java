package com.ttgint.library.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ValidationComponent {

    DEFAULT_VALIDATION("DEFAULT_VALIDATION");

    @JsonValue
    private final String value;

}
