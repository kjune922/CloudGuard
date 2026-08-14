package com.cloudguard.cloudguard.budget.service;

import com.cloudguard.cloudguard.budget.domain.BudgetPolicy;
import com.cloudguard.cloudguard.budget.domain.BudgetStatus;
import com.cloudguard.cloudguard.budget.domain.MonthlyBudget;
import com.cloudguard.cloudguard.budget.exception.DuplicateMonthlyBudgetException;
import com.cloudguard.cloudguard.budget.repository.MonthlyBudgetRepository;
import com.cloudguard.cloudguard.cost.service.CostService;
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

    public BudgetStatus determineMonthlyStatus(YearMonth yearMonth, BigDecimal monthlyLimit){
        BigDecimal monthlyCost = costService.calculateMonthlyCost(yearMonth);

        BudgetPolicy budgetPolicy = new BudgetPolicy(monthlyLimit);
        return budgetPolicy.determineStatus(monthlyCost);
    }

    public MonthlyBudget addMonthlyBudget(YearMonth yearMonth,BigDecimal monthlyLimit){
        if(monthlyBudgetRepository.existsByYearMonth(yearMonth)){
            throw new DuplicateMonthlyBudgetException();
        }

        MonthlyBudget monthlyBudget = new MonthlyBudget(yearMonth,monthlyLimit);

        return monthlyBudgetRepository.save(monthlyBudget);
    }
}
