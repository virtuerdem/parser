package com.ttgint.library.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TimeUnit {

    YEARS("YEARS"),
    MONTHS("MONTHS"),
    WEEKS("WEEKS"),
    DAYS("DAYS"),
    HOURS("HOURS"),
    MINUTES("MINUTES"),
    SECONDS("SECONDS");

    @JsonValue
    private final String value;

}
