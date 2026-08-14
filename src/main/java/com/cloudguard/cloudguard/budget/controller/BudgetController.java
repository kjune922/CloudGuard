package com.cloudguard.cloudguard.budget.controller;


import com.cloudguard.cloudguard.budget.domain.BudgetStatus;
import com.cloudguard.cloudguard.budget.domain.MonthlyBudget;
import com.cloudguard.cloudguard.budget.dto.BudgetCreateRequest;
import com.cloudguard.cloudguard.budget.service.BudgetService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping("/status")
    public BudgetStatus determineMonthlyStatus(
            @RequestParam("yearMonth")
            @DateTimeFormat (pattern = "yyyy-MM")
            YearMonth yearMonth
    ){
            return budgetService.determineMonthlyStatus(yearMonth);
    }

    @PostMapping("/add")
    public MonthlyBudget addMonthlyBudget(@RequestBody BudgetCreateRequest request){
        return budgetService.addMonthlyBudget(request.getYearMonth(),request.getMonthlyLimit());
    }
}
