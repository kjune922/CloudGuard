package com.cloudguard.cloudguard.budget.domain;

import com.cloudguard.cloudguard.budget.converter.YearMonthConverter;
import com.cloudguard.cloudguard.budget.exception.WrongRequestBadRequestException;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.YearMonth;

/**
 * 1. DB에 저장하는 2개중 1개인 MonthlyBudget
 * "특정 월의 예산 한건"
 */

@Entity
@Table(name = "monthly_budgets")
public class MonthlyBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Convert(converter = YearMonthConverter.class)
    @Column(name = "budget_month",nullable = false,unique = true,length = 7)
    private YearMonth yearMonth;

    @Column(name = "monthly_limit", nullable = false, precision = 38, scale = 18)
    private BigDecimal monthlyLimit; // 월 예산

    protected MonthlyBudget() {
    }

    public MonthlyBudget(YearMonth yearMonth, BigDecimal monthlyLimit) {
        validateYearMonth(yearMonth);
        validateMonthlyLimit(monthlyLimit);
        this.yearMonth = yearMonth;
        this.monthlyLimit = monthlyLimit;
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getMonthlyLimit() {
        return monthlyLimit;
    }

    public YearMonth getYearMonth() {
        return yearMonth;
    }

    private void validateYearMonth(YearMonth yearMonth){
        if(yearMonth == null){
            throw new IllegalArgumentException("연월은 필수입니다.");
        }
    }

    private void validateMonthlyLimit(BigDecimal monthlyLimit){
        if(monthlyLimit == null || monthlyLimit.compareTo(BigDecimal.ZERO) <= 0){
            throw new WrongRequestBadRequestException();
        }
    }

    // 월예산 변경 메소드
    public void updateMonthlyLimit(BigDecimal monthlyLimit){
        validateMonthlyLimit(monthlyLimit);
        this.monthlyLimit = monthlyLimit;
    }
}
