package com.cloudguard.cloudguard.budget.exception;

public class DuplicateMonthlyBudgetException extends RuntimeException{

    public DuplicateMonthlyBudgetException() {
        super("해당 연월의 예산이 이미 등록되어 있습니다.");
    }
}
