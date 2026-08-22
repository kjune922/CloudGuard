package com.cloudguard.cloudguard.cost.dto;

import java.math.BigDecimal;
import java.time.YearMonth;

public class MonthlyCostResponse {

    private final YearMonth yearMonth;
    private final BigDecimal totalCost;

    public MonthlyCostResponse(
            YearMonth yearMonth,
            BigDecimal totalCost
    ) {
        this.yearMonth = yearMonth;
        this.totalCost = totalCost;
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }
}
