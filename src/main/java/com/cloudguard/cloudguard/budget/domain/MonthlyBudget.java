package com.cloudguard.cloudguard.budget.domain;

import com.cloudguard.cloudguard.budget.converter.YearMonthConverter;
import jakarta.persistence.*;
import org.hibernate.type.YesNoConverter;

import java.math.BigDecimal;
import java.time.YearMonth;


@Entity
@Table(name = "monthly_budgets")
public class MonthlyBudget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Convert(converter = YearMonthConverter.class)
    @Column(name = "year_month",nullable = false,unique = true,length = 7)
    private YearMonth yearMonth;

    @Column(name = "monthly_limit",nullable = false)
    private BigDecimal monthlyLimit;

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
            throw new IllegalArgumentException("월 예산은 0보다 커야합니다.");
        }
    }
}
