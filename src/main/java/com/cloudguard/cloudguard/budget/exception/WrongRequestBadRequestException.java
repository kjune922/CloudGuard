package com.cloudguard.cloudguard.budget.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class WrongRequestBadRequestException extends RuntimeException {

    public WrongRequestBadRequestException() {
        super("월 예산은 0보다 커야 합니다.");
    }
}
