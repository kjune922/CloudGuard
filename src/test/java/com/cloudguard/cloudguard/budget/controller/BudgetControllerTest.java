package com.cloudguard.cloudguard.budget.controller;

import com.cloudguard.cloudguard.budget.domain.BudgetStatus;
import com.cloudguard.cloudguard.budget.domain.MonthlyBudget;
import com.cloudguard.cloudguard.budget.dto.BudgetStatusResponse;
import com.cloudguard.cloudguard.budget.exception.WrongRequestBadRequestException;
import com.cloudguard.cloudguard.budget.service.BudgetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.cloudguard.cloudguard.budget.exception.DuplicateMonthlyBudgetException;
import com.cloudguard.cloudguard.budget.exception.MonthlyBudgetNotFoundException;
import java.math.BigDecimal;
import java.time.YearMonth;

import static org.mockito.BDDMockito.given;


@WebMvcTest(BudgetController.class)
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BudgetService budgetService;

    @Test
    void 월_예산_상태_조회() throws Exception {
        YearMonth yearMonth = YearMonth.of(2026,8);

        BudgetStatusResponse response = new BudgetStatusResponse(
                yearMonth,
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(800),
                BigDecimal.valueOf(80),
                BudgetStatus.CAUTION
        );

        given(budgetService.determineMonthlyStatusDetail(
                yearMonth
        )).willReturn(response);

        mockMvc.perform(get("/api/budgets/status")
                        .param("yearMonth", "2026-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yearMonth")
                        .value("2026-08"))
                .andExpect(jsonPath("$.monthlyLimit")
                        .value(1000))
                .andExpect(jsonPath("$.totalCost")
                        .value(800))
                .andExpect(jsonPath("$.usageRate")
                        .value(80))
                .andExpect(jsonPath("$.status")
                        .value("CAUTION"));

        verify(budgetService).determineMonthlyStatusDetail(
                yearMonth
        );
    }

    @Test
    void 월_예산_등록() throws Exception{
        YearMonth yearMonth = YearMonth.of(2026,8);
        BigDecimal monthlyLimit = BigDecimal.valueOf(1000);

        MonthlyBudget savedBudget = new MonthlyBudget(yearMonth,monthlyLimit);

        given(budgetService.addMonthlyBudget(yearMonth,monthlyLimit))
                .willReturn(savedBudget);

        mockMvc.perform(post("/api/budgets/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "yearMonth": "2026-08",
                            "monthlyLimit": 1000
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yearMonth").value("2026-08"))
                .andExpect(jsonPath("$.monthlyLimit").value(1000));

        verify(budgetService).addMonthlyBudget(
                yearMonth,
                monthlyLimit
        );
    }

    @Test
    void 등록되지않은_연월의_상태조회는_404() throws Exception{
        YearMonth yearMonth = YearMonth.of(2026,10);

        given(budgetService.determineMonthlyStatusDetail(yearMonth))
                .willThrow(new MonthlyBudgetNotFoundException());

        mockMvc.perform(get("/api/budgets/status")
                        .param("yearMonth", "2026-10"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code")
                        .value("MONTHLY_BUDGET_NOT_FOUND"))
                .andExpect(jsonPath("$.message")
                        .value("해당 연월의 예산이 등록되어 있지 않습니다."))
                .andExpect(jsonPath("$.path")
                        .value("/api/budgets/status"));

        verify(budgetService).determineMonthlyStatusDetail(yearMonth);
    }

    @Test
    void 등록되지_않은_연월의_예산변경은_404() throws Exception {
        YearMonth yearMonth = YearMonth.of(2050, 10);
        BigDecimal updateLimit = BigDecimal.valueOf(2000);

        given(budgetService.updateMonthlyBudget(yearMonth, updateLimit))
                .willThrow(new MonthlyBudgetNotFoundException());

        mockMvc.perform(put("/api/budgets/2050-10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                            {
                                "monthlyLimit": 2000
                            }
                            """))
                .andExpect(status().isNotFound());

        verify(budgetService).updateMonthlyBudget(
                yearMonth,
                updateLimit
        );
    }

    @Test
    void 같은_연월의_예산을_중복등록하면_409() throws Exception {
        YearMonth yearMonth1 = YearMonth.of(2026,8);
        BigDecimal monthlyLimit = BigDecimal.valueOf(1000);

        given(budgetService.addMonthlyBudget(yearMonth1,monthlyLimit))
                .willThrow(new DuplicateMonthlyBudgetException());

        mockMvc.perform(post("/api/budgets/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "yearMonth": "2026-08",
                            "monthlyLimit": 1000
                        }
                        """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code")
                        .value("DUPLICATE_MONTHLY_BUDGET"))
                .andExpect(jsonPath("$.message")
                        .value("해당 연월의 예산이 이미 등록되어 있습니다."))
                .andExpect(jsonPath("$.path")
                        .value("/api/budgets/add"));

        verify(budgetService).addMonthlyBudget(
                yearMonth1,
                monthlyLimit
        );
    }

    @Test
    void 월_예산을_변경 () throws Exception{
        YearMonth yearMonth = YearMonth.of(2026,8);
        BigDecimal updateLimit = BigDecimal.valueOf(2000);

        MonthlyBudget updatedBudget = new MonthlyBudget(yearMonth,updateLimit);

        given(budgetService.updateMonthlyBudget(
                yearMonth,
                updateLimit
        )).willReturn(updatedBudget);

        mockMvc.perform(put("/api/budgets/2026-08")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                        "monthlyLimit": 2000
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yearMonth")
                        .value("2026-08"))
                .andExpect(jsonPath("$.monthlyLimit")
                        .value(2000));

        verify(budgetService).updateMonthlyBudget(
                yearMonth,
                updateLimit
        );
    }
    @Test
    void 월_예산을_0이하로_변경하거나_설정하면_400() throws Exception{
        YearMonth yearMonth = YearMonth.of(2026,8);
        BigDecimal badUpdateLimit = BigDecimal.valueOf(0);

        given(budgetService.updateMonthlyBudget(yearMonth,badUpdateLimit))
                .willThrow(new WrongRequestBadRequestException());

        mockMvc.perform(put("/api/budgets/2026-08")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "monthlyLimit" : 0
                        }
                        """))
                .andExpect(status().isBadRequest());

        verify(budgetService).updateMonthlyBudget(
                yearMonth,
                badUpdateLimit
        );
    }
}