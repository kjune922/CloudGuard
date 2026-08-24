package com.cloudguard.cloudguard.budget.exception;

public class WrongRequestBadRequestException extends RuntimeException {

    public WrongRequestBadRequestException() {
        super("월 예산은 0보다 커야 합니다.");
    }
}
