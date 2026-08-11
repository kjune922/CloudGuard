package com.cloudguard.cloudguard.budget.service;

import com.cloudguard.cloudguard.budget.domain.BudgetPolicy;
import com.cloudguard.cloudguard.budget.domain.BudgetStatus;
import com.cloudguard.cloudguard.cost.service.CostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.YearMonth;

@Service
public class BudgetService {

    private final CostService costService;

    @Autowired
    public BudgetService(CostService costService) {
        this.costService = costService;
    }

    public BudgetStatus determineMonthlyStatus(YearMonth yearMonth, BigDecimal monthlyLimit){
        BigDecimal monthlyCost = costService.calculateMonthlyCost(yearMonth);

        BudgetPolicy budgetPolicy = new BudgetPolicy(monthlyLimit);

        return budgetPolicy.determineStatus(monthlyCost);
    }
}
