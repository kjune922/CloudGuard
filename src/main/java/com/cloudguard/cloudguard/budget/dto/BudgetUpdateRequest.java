package com.cloudguard.cloudguard.budget.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public class BudgetUpdateRequest {

    @NotNull(message = "월 예산은 필수입니다.")
    @Positive(message = "월 예산은 0보다 커야 합니다.")
    private BigDecimal monthlyLimit;

    public BudgetUpdateRequest (){
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }
}
