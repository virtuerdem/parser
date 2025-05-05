package com.ttgint.library.exception;

import lombok.Data;

@Data
public class EtlException extends RuntimeException {

    private final String code;
    private final String detail;

    public EtlException(String code, String detail) {
        super("Notification Code: " + code + ", Detail: " + detail);
        this.code = code;
        this.detail = detail;
    }

}