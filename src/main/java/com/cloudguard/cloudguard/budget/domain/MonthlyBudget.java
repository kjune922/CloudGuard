package com.cloudguard.cloudguard.budget.domain;

import com.cloudguard.cloudguard.validator.MonthlyLimitValidator;
import com.cloudguard.cloudguard.validator.YearMonthValidator;
import java.math.BigDecimal;
import java.time.YearMonth;

public class MonthlyBudget {

    private final YearMonth yearMonth;
    private final BigDecimal monthlyLimit;
    private final YearMonthValidator yearMonthValidator = new YearMonthValidator();
    private final MonthlyLimitValidator monthlyLimitValidator = new MonthlyLimitValidator();


    public MonthlyBudget(YearMonth yearMonth, BigDecimal monthlyLimit) {
        yearMonthValidator.validateYearMonth(yearMonth);
        monthlyLimitValidator.validateMonthlyLimit(monthlyLimit);

        this.yearMonth = yearMonth;
        this.monthlyLimit = monthlyLimit;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }
}
