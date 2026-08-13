package com.cloudguard.cloudguard.budget.domain;

import java.math.BigDecimal;
import java.time.YearMonth;

public class MonthlyBudget {

    private final YearMonth yearMonth;
    private final BigDecimal monthlyLimit;


    public MonthlyBudget(YearMonth yearMonth, BigDecimal monthlyLimit) {
        validateYearMonth(yearMonth);
        validateMonthlyLimit(monthlyLimit);
        this.yearMonth = yearMonth;
        this.monthlyLimit = monthlyLimit;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    private void validateYearMonth(YearMonth yearMonth){
        if(yearMonth == null){
            throw new IllegalArgumentException("연월은 필수입니다.");
        }
    }

    private void validateMonthlyLimit(BigDecimal monthlyLimit){
        if(monthlyLimit == null || monthlyLimit.compareTo(BigDecimal.ZERO) <= 0){
            throw new IllegalArgumentException("월 예산은 0보다 커야합니다.");
        }
    }
}
