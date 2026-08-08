package com.cloudguard.cloudguard.budget.domain;

import com.cloudguard.cloudguard.budget.BudgetStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class BudgetPolicy {

    private final BigDecimal monthlyLimit;


    public BigDecimal getMonthlyLimit(){
        return monthlyLimit;
    }

    public BudgetPolicy(BigDecimal monthlyLimit) {
        validateMonthlyLimit(monthlyLimit);
        this.monthlyLimit = monthlyLimit;
    }

    public BudgetStatus determineStatus(BigDecimal currentCost){
        validateCurrentCost(currentCost);

        BigDecimal usageRate = calculateUsageRate(currentCost);

        if(usageRate.compareTo(BigDecimal.valueOf(100)) >= 0){
            return BudgetStatus.EXCEEDED;
        }

        if(usageRate.compareTo(BigDecimal.valueOf(85)) >= 0){
            return BudgetStatus.WARNING;
        }

        if(usageRate.compareTo(BigDecimal.valueOf(70)) >= 0){
            return BudgetStatus.CAUTION;
        }

        return BudgetStatus.SAFE;
    }

    private void validateMonthlyLimit(BigDecimal monthlyLimit) {
        if (monthlyLimit == null || monthlyLimit.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("월 예산은 0보다 커야 합니다 !!");
        }
    }

    private void validateCurrentCost(BigDecimal currentCost) {
        if(currentCost == null || currentCost.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("현재 비용은 0 이상이어야 합니다 !!");
        }
    }

    private BigDecimal calculateUsageRate(BigDecimal currentCost) {
        validateCurrentCost(currentCost);

        return currentCost
                .divide(monthlyLimit, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

}
