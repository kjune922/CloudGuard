package com.cloudguard.cloudguard.budget.controller;

import com.cloudguard.cloudguard.budget.domain.BudgetStatus;
import com.cloudguard.cloudguard.budget.domain.MonthlyBudget;
import com.cloudguard.cloudguard.budget.repository.MonthlyBudgetRepository;
import com.cloudguard.cloudguard.budget.service.BudgetService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

        given(budgetService.determineMonthlyStatus(
                yearMonth
        )).willReturn(BudgetStatus.CAUTION);

        mockMvc.perform(get("/api/budgets/status")
                .param("yearMonth","2026-08"))
                .andExpect(status().isOk())
                .andExpect(content().json("\"CAUTION\""));
        verify(budgetService).determineMonthlyStatus(
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

        given(budgetService.determineMonthlyStatus(yearMonth))
                .willThrow(new MonthlyBudgetNotFoundException());

        mockMvc.perform(get("/api/budgets/status")
                .param("yearMonth","2026-10"))
                .andExpect(status().isNotFound());

        verify(budgetService).determineMonthlyStatus(yearMonth);
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
                            "yearMonth" : "2026-08",
                            "monthlyLimit" : 1000
                        }
                        """))
                .andExpect(status().isConflict());

        verify(budgetService).addMonthlyBudget(
                yearMonth1,
                monthlyLimit
        );
    }

}