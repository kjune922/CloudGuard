package com.cloudguard.cloudguard.cost.domain;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

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

        BigDecimal total = BigDecimal.ZERO;

        for(CostRecord record : costRecords){
            if(YearMonth.from(record.getUsageDate()).equals(yearMonth)){
                total = total.add(record.getCost());
            }
        }
        return total;
    }
}
