package com.cloudguard.cloudguard.common.exception;

import com.cloudguard.cloudguard.budget.exception.MonthlyBudgetNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice // RestControllerAdvice는 왜 붙이고 어디에 쓰는거지?
public class GlobalExceptionHandler {

    @ExceptionHandler(MonthlyBudgetNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMonthlyBudgetNotFound(
            MonthlyBudgetNotFoundException exception,
            HttpServletRequest request
    ) {
        HttpStatus status = HttpStatus.NOT_FOUND;

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                "MONTHLY_BUDGET_NOT_FOUND",
                exception.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(response);
    }
}
