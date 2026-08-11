package com.cloudguard.cloudguard.cost.dto;

import com.cloudguard.cloudguard.cost.domain.CloudService;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CostCreateRequest {

    private CloudService cloudService;
    private BigDecimal cost;
    private LocalDate usageDate;

    public CostCreateRequest() {
    }

    public CloudService getCloudService() {
        return cloudService;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public LocalDate getUsageDate() {
        return usageDate;
    }
}
