package com.cloudguard.cloudguard.cost.dto;

import com.cloudguard.cloudguard.cost.domain.CloudService;

import java.math.BigDecimal;
import java.time.YearMonth;

public class MonthlyServiceCostResponse {

    private final YearMonth yearMonth;
    private final CloudService service;
    private final BigDecimal totalCost;

    public MonthlyServiceCostResponse(
            YearMonth yearMonth,
            CloudService service,
            BigDecimal totalCost
    ) {
        this.yearMonth = yearMonth;
        this.service = service;
        this.totalCost = totalCost;
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public CloudService getService() {
        return service;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }
}