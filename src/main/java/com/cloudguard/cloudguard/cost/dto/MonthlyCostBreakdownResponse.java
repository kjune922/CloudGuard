package com.cloudguard.cloudguard.cost.dto;

import com.cloudguard.cloudguard.cost.domain.CloudService;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

public class MonthlyCostBreakdownResponse {

    private final YearMonth yearMonth; // 조회할 연월
    private final BigDecimal totalCost; // 모든 서비스 비용의 총합
    private final Map<CloudService, BigDecimal> serviceCosts; // EC2, RDS, S3 각각의 비용합계

    public MonthlyCostBreakdownResponse(YearMonth yearMonth, BigDecimal totalCost, Map<CloudService, BigDecimal> serviceCosts) {
        this.yearMonth = yearMonth;
        this.totalCost = totalCost;
        this.serviceCosts = serviceCosts;
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }

    public Map<CloudService, BigDecimal> getServiceCosts() {
        return serviceCosts;
    }
}
