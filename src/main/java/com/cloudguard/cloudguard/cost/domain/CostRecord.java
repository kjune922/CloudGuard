package com.cloudguard.cloudguard.cost.domain;

import com.cloudguard.cloudguard.cost.CloudService;

import java.math.BigDecimal;
import java.time.LocalDate;

public class CostRecord {

    private final CloudService service; // 사용한 서비스
    private final BigDecimal cost; // 비용
    private final LocalDate usageDate; // 비용발생일


    public CostRecord(CloudService service, BigDecimal cost, LocalDate usageDate) {
        validateService(service);
        validateCost(cost);
        validateUsageDate(usageDate);
        this.service = service;
        this.cost = cost;
        this.usageDate = usageDate;
    }

    private void validateService(CloudService service) {

        if(service == null){
            throw new IllegalArgumentException("클라우드 서비스는 필수입니다");
        }

    }

    private void validateCost(BigDecimal cost) {

        if(cost == null){
            throw new IllegalArgumentException("비용은 필수 입니다");
        }
        if(cost.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("비용은 음수일 수 없습니다.");
        }
    }

    private void validateUsageDate(LocalDate usageDate) {
        if(usageDate == null){
            throw new IllegalArgumentException("비용 발생일은 필수입니다.");
        }
    }

    public CloudService getService() {
        return service;
    }

    public BigDecimal getCost() {
        return cost;
    }

    public LocalDate getUsageDate() {
        return usageDate;
    }
}
