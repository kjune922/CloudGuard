package com.cloudguard.cloudguard.cost.aws.dto;

import java.math.BigDecimal;

public class AwsServiceCost {

    private final String serviceName;
    private final BigDecimal amount;
    private final String unit;

    public AwsServiceCost(String serviceName, BigDecimal amount, String unit) {
        this.serviceName = serviceName;
        this.amount = amount;
        this.unit = unit;
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
}
