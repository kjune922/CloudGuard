package com.cloudguard.cloudguard.budget.service;

import com.cloudguard.cloudguard.budget.domain.BudgetPolicy;
import com.cloudguard.cloudguard.budget.domain.BudgetStatus;
import com.cloudguard.cloudguard.budget.domain.MonthlyBudget;
import com.cloudguard.cloudguard.budget.dto.BudgetStatusResponse;
import com.cloudguard.cloudguard.budget.exception.DuplicateMonthlyBudgetException;
import com.cloudguard.cloudguard.budget.exception.MonthlyBudgetNotFoundException;
import com.cloudguard.cloudguard.budget.repository.MonthlyBudgetRepository;
import com.cloudguard.cloudguard.cost.service.CostService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;

@Service
public class BudgetService {

    private final CostService costService;
    private final MonthlyBudgetRepository monthlyBudgetRepository;

    @Autowired
    public BudgetService(CostService costService, MonthlyBudgetRepository monthlyBudgetRepository) {
        this.costService = costService;
        this.monthlyBudgetRepository = monthlyBudgetRepository;
    }

    public MonthlyBudget addMonthlyBudget(YearMonth yearMonth,BigDecimal monthlyLimit){
        if(monthlyBudgetRepository.existsByYearMonth(yearMonth)){
            throw new DuplicateMonthlyBudgetException();
        }

        MonthlyBudget monthlyBudget = new MonthlyBudget(yearMonth,monthlyLimit);

        return monthlyBudgetRepository.save(monthlyBudget);
    }

    @Transactional
    public MonthlyBudget updateMonthlyBudget(YearMonth yearMonth,BigDecimal monthlyLimit) {
        MonthlyBudget monthlyBudget = monthlyBudgetRepository
                .findByYearMonth(yearMonth)
                .orElseThrow(MonthlyBudgetNotFoundException::new);

        monthlyBudget.updateMonthlyLimit(monthlyLimit);

        return monthlyBudget;
    }

    public BudgetStatus determineMonthlyStatus(YearMonth yearMonth){
        // 상세 상태 메소드에게 위임
        // 이러면 예산 및 조회 계산 코드를 두 메소드에 중복되지않게 가능
        return determineMonthlyStatusDetail(yearMonth).getStatus();
    }

    // 상세 상태 메소드
    public BudgetStatusResponse determineMonthlyStatusDetail(YearMonth yearMonth) {
        MonthlyBudget monthlyBudget = monthlyBudgetRepository
                .findByYearMonth(yearMonth)
                .orElseThrow(MonthlyBudgetNotFoundException::new);

        BigDecimal totalCost=  costService.calculateMonthlyCost(yearMonth);

        BudgetPolicy budgetPolicy = new BudgetPolicy(monthlyBudget.getMonthlyLimit());

        BigDecimal usageRate = budgetPolicy.calculateUsageRate(totalCost);

        BudgetStatus status = budgetPolicy.determineStatus(totalCost);

        return new BudgetStatusResponse(
                yearMonth,
                monthlyBudget.getMonthlyLimit(),
                totalCost,
                usageRate,
                status
        );
    }
}
