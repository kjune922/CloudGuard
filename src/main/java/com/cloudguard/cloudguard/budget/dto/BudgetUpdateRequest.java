package com.cloudguard.cloudguard.budget.dto;

import java.math.BigDecimal;

public class BudgetUpdateRequest {

    private BigDecimal monthlyLimit;

    public BudgetUpdateRequest (){
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }
}
