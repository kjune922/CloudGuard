package com.cloudguard.cloudguard.cost.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
public class CostRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private CloudService service; // 사용한 서비스
    private BigDecimal cost; // 비용
    private LocalDate usageDate; // 비용발생일
    @Enumerated(EnumType.STRING)
    private CostSource source;

    protected CostRecord() {
    }

    public CostRecord(CloudService service, BigDecimal cost, LocalDate usageDate){
        this(service,cost,usageDate, CostSource.MANUAL);
    }

    public CostRecord(CloudService service, BigDecimal cost, LocalDate usageDate, CostSource source) {
        validateService(service);
        validateCost(cost);
        validateUsageDate(usageDate);
        validateSource(source);
        this.service = service;
        this.cost = cost;
        this.usageDate = usageDate;
        this.source = source;
    }

    public void updateCost(BigDecimal cost){
        validateCost(cost);
        this.cost = cost;
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

    private void validateSource(CostSource source){
        if(source == null){
            throw new IllegalArgumentException("비용 출저는 필수입니다.");
        }
    }

    public CostSource getSource() {
        return source;
    }

    public Long getId(){
        return id;
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
