package com.cloudguard.cloudguard.cost.domain;

import java.math.BigDecimal;
import java.time.Year;
import java.time.YearMonth;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class MonthlyCost {

    private final List<CostRecord> costRecords;

    public MonthlyCost(List<CostRecord> costRecords) {

        if(costRecords == null){
            throw new IllegalArgumentException("비용 기록 목록은 필수입니다.");
        }
        this.costRecords = costRecords;
    }

    public BigDecimal calculateTotal(YearMonth yearMonth){
        if(yearMonth == null){
            throw new IllegalArgumentException("조회할 연월은 필수입니다.");
        }

        BigDecimal total = BigDecimal.ZERO; // 총합total은 0부터 시작

        for(CostRecord record : costRecords){
            if(YearMonth.from(record.getUsageDate()).equals(yearMonth)){
                total = total.add(record.getCost());
            }
        }
        return total;
    }

    public Map<CloudService,BigDecimal> calculateTotalByService(YearMonth yearMonth){
        if(yearMonth == null){
            throw new IllegalArgumentException("조회할 연월은 필수입니다.");
        }


        // EnumMap에 대한 개념 숙지
        Map<CloudService, BigDecimal> totals = new EnumMap<>(CloudService.class);

        // EnumMap이라 CloudService를 Enum으로 선언해서 그 values들을 쓰는건가?
        for (CloudService cloudService : CloudService.values()) {
            totals.put(cloudService, BigDecimal.ZERO);
        }

        for (CostRecord costRecord : costRecords) {
            YearMonth usageMonth = YearMonth.from(costRecord.getUsageDate());

            // merge 메소드에 대한 숙지, BigDecimal::add 함수에 대한 숙지 필요
            if(usageMonth.equals(yearMonth)){
                totals.merge(
                        costRecord.getService(),
                        costRecord.getCost(),
                        BigDecimal::add
                );
            }
        }
        return totals;
    }
}
