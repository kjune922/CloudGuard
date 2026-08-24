package com.cloudguard.cloudguard.budget.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.YearMonth;


/**
 *  Data Transfer Obejct 의 역할은 사용자가 보낸 JSON 을 자바객체로 받기
 *  기본 생성자는 Jackson이 JSON을 객체로 변환할 때 사용
 */
public class BudgetCreateRequest {

    @NotNull(message = "연월은 필수입니다.") // 값이 누락되거나 null인 경우 거부
    private YearMonth yearMonth;

    @NotNull(message = "월 예산은 필수입니다.")
    @Positive(message = "월 예산은 0보다 커야 합니다.") // 숫자가 0또는 음수 인 경우 거부
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
