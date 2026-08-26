package com.cloudguard.cloudguard.cost.dto;

import com.cloudguard.cloudguard.cost.domain.CloudService;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CostCreateRequest {

    @NotNull(message = "클라우드 서비스는 필수입니다.")
    private CloudService cloudService;

    @NotNull(message = "비용은 필수입니다.")
    @PositiveOrZero(message = "비용은 음수일 수 없습니다.")
    private BigDecimal cost;

    @NotNull(message = "비용 발생일은 필수입니다.")
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
