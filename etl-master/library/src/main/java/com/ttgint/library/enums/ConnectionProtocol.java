package com.ttgint.library.enums;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ConnectionProtocol {

    FTP("FTP"),
    SFTP("SFTP"),
    CP("CP");

    @JsonValue
    private final String value;

}
