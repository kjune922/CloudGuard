package com.cloudguard.cloudguard.budget.dto;

import com.cloudguard.cloudguard.budget.domain.BudgetStatus;

import java.math.BigDecimal;
import java.time.YearMonth;

public class BudgetStatusResponse {

    private final YearMonth yearMonth;
    private final BigDecimal monthlyLimit;
    private final BigDecimal totalCost;
    private final BigDecimal usageRate;
    private final BudgetStatus status;


    public BudgetStatusResponse(YearMonth yearMonth, BigDecimal monthlyLimit, BigDecimal totalCost, BigDecimal usageRate, BudgetStatus status) {
        this.yearMonth = yearMonth;
        this.monthlyLimit = monthlyLimit;
        this.totalCost = totalCost;
        this.usageRate = usageRate;
        this.status = status;
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public BigDecimal getUsageRage() {
        return usageRate;
    }

    public BudgetStatus getStatus() {
        return status;
    }
}
