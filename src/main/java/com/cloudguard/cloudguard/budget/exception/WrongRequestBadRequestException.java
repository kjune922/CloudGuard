package com.cloudguard.cloudguard.budget.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class WrongRequestBadRequestException extends RuntimeException {

    public WrongRequestBadRequestException() {
        super("잘못된 수정 요청입니다");
    }
}
