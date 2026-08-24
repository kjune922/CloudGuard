package com.cloudguard.cloudguard.budget.controller;

import com.cloudguard.cloudguard.budget.domain.MonthlyBudget;
import com.cloudguard.cloudguard.budget.dto.BudgetCreateRequest;
import com.cloudguard.cloudguard.budget.dto.BudgetStatusResponse;
import com.cloudguard.cloudguard.budget.dto.BudgetUpdateRequest;
import com.cloudguard.cloudguard.budget.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.YearMonth;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    // 기존 상태 -> 상세 상태반환으로 업데이트
    @GetMapping("/status")
    public BudgetStatusResponse determineMonthlyStatus(
            @RequestParam("yearMonth")
            @DateTimeFormat (pattern = "yyyy-MM")
            YearMonth yearMonth
    ){
            return budgetService.determineMonthlyStatusDetail(
                    yearMonth
            );
    }

    @PostMapping("/add")
    public MonthlyBudget addMonthlyBudget(
            @Valid @RequestBody BudgetCreateRequest request
    ){
        return budgetService.addMonthlyBudget(request.getYearMonth(),request.getMonthlyLimit());
    }

    @PutMapping("/{yearMonth}")
    public MonthlyBudget updateMonthlyBudget(
            @PathVariable("yearMonth")
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            @Valid @RequestBody BudgetUpdateRequest request) {

        return budgetService.updateMonthlyBudget(yearMonth,request.getMonthlyLimit());
    }
}
