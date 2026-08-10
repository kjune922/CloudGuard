package com.cloudguard.cloudguard.budget.domain;


/** 예산 상태 체크 = BudgetStatus
 * 안전
 * 위험
 * 경고
 * 초과
 */
public enum BudgetStatus {
    SAFE,
    CAUTION,
    WARNING,
    EXCEEDED
}
