package com.cloudguard.cloudguard.budget.controller;


import com.cloudguard.cloudguard.budget.domain.BudgetStatus;
import com.cloudguard.cloudguard.budget.service.BudgetService;
import com.cloudguard.cloudguard.cost.service.CostService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;
    private final CostService costService;

    public BudgetController(BudgetService budgetService, CostService costService) {
        this.budgetService = budgetService;
        this.costService = costService;
    }

    @GetMapping("/status")
    public BudgetStatus determineMonthlyStatus(
            @RequestParam("yearMonth")
            @DateTimeFormat (pattern = "yyyy-MM")
            YearMonth yearMonth,
            @RequestParam("monthlyLimit")
            BigDecimal monthlyLimit
    ){
            return budgetService.determineMonthlyStatus(yearMonth,monthlyLimit);
    }
}
