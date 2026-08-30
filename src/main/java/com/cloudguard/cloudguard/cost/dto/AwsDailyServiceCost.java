package com.cloudguard.cloudguard.cost.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AwsDailyServiceCost {

    private final String serviceName;
    private final BigDecimal amount;
    private final String unit;
    private final LocalDate usageDate;

    public AwsDailyServiceCost(String serviceName, BigDecimal amount, String unit, LocalDate usageDate) {
        this.serviceName = serviceName;
        this.amount = amount;
        this.unit = unit;
        this.usageDate = usageDate;
    }

    public String getServiceName() {
        return serviceName;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getUnit() {
        return unit;
    }

    public LocalDate getUsageDate() {
        return usageDate;
    }
}
