package com.aquamanager.shared.domain.exception;

/** Exceção base para violações de regra de negócio (HTTP 422/400). */
public class BusinessException extends RuntimeException {

    private final String code;

    public BusinessException(String code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this("BUSINESS_RULE_VIOLATION", message);
    }

    public String getCode() {
        return code;
    }
}
