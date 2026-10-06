package com.cloudguard.cloudguard.budget.controller;

import com.cloudguard.cloudguard.budget.domain.MonthlyBudget;
import com.cloudguard.cloudguard.budget.dto.BudgetCreateRequest;
import com.cloudguard.cloudguard.budget.dto.BudgetStatusResponse;
import com.cloudguard.cloudguard.budget.dto.BudgetUpdateRequest;
import com.cloudguard.cloudguard.budget.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.YearMonth;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

//    public BudgetController(BudgetService budgetService) {
//        this.budgetService = budgetService;
//    }

    // 월 비용과 예산을 비교한 상세 상태 조회 메서드
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

    // 월 예산 등록 메서드
    @PostMapping("/add")
    public MonthlyBudget addMonthlyBudget(
            @Valid @RequestBody BudgetCreateRequest request
    ){
        return budgetService.addMonthlyBudget(request.getYearMonth(),request.getMonthlyLimit());
    }

    // 해당 월의 예산 변경 메서드
    @PutMapping("/{yearMonth}")
    public MonthlyBudget updateMonthlyBudget(
            @PathVariable("yearMonth")
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth yearMonth,
            @Valid @RequestBody BudgetUpdateRequest request) {

        return budgetService.updateMonthlyBudget(yearMonth,request.getMonthlyLimit());
    }
}
