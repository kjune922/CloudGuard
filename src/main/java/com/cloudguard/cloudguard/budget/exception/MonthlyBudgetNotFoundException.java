package com.cloudguard.cloudguard.budget.exception;

public class MonthlyBudgetNotFoundException extends RuntimeException{

    public MonthlyBudgetNotFoundException() {
        super("해당 연월의 예산이 등록되어 있지 않습니다.");
    }
}
