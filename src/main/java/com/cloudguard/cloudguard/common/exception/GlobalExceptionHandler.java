package com.cloudguard.cloudguard.common.exception;

import com.cloudguard.cloudguard.budget.exception.DuplicateMonthlyBudgetException;
import com.cloudguard.cloudguard.budget.exception.MonthlyBudgetNotFoundException;
import com.cloudguard.cloudguard.budget.exception.WrongRequestBadRequestException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MonthlyBudgetNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMonthlyBudgetNotFound(
            MonthlyBudgetNotFoundException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "MONTHLY_BUDGET_NOT_FOUND",
                exception,
                request
        );
    }

    @ExceptionHandler(DuplicateMonthlyBudgetException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateMonthlyBudget (
            DuplicateMonthlyBudgetException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "DUPLICATE_MONTHLY_BUDGET",
                exception,
                request
        );
    }

    @ExceptionHandler(WrongRequestBadRequestException.class)
    public ResponseEntity<ErrorResponse> handleWrongRequest(
            WrongRequestBadRequestException exception,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "INVALID_MONTHLY_LIMIT",
                exception,
                request
        );
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String code,
            RuntimeException exception,
            HttpServletRequest request
    ) {
        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                code,
                exception.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(response);
    }
}
