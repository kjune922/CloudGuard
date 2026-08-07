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

        BigDecimal total = BigDecimal.ZERO; // 총합total은 0부터 시작

        /** <cost.compareTo(BigDecimal.ZERO)>
         * 음수 -> cost < 0
         * 0 -> cost = 0
         * 양수 -> cost > 0
         */

        /** <YearMonth> 연도 & 월만 표기하는 클래스
         * LocalDate.of(2026,9,22) -> 2026년 9월 22일
         * YearMonth.of(2026,8) -> 2026년 8월
         * YearMonth.from() 은 다른 날짜 객체에서 "연도와 월" 만 뽑아내는 메서드
         */

        for(CostRecord record : costRecords){
            if(YearMonth.from(record.getUsageDate()).equals(yearMonth)){
                total = total.add(record.getCost());
            }
        }
        return total;
    }
}
