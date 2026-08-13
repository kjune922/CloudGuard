package com.cloudguard.cloudguard.budget.dto;

import java.math.BigDecimal;
import java.time.YearMonth;


/**
 *  Data Transfer Obejct 의 역할은 사용자가 보낸 JSON 을 자바객체로 받기
 *  기본 생성자는 Jackson이 JSON을 객체로 변환할 때 사용
 */
public class BudgetCreateRequest {

    private YearMonth yearMonth;
    private BigDecimal monthlyLimit;

    public BudgetCreateRequest() {
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }
}
