package com.cloudguard.cloudguard.budget.controller;

import com.cloudguard.cloudguard.budget.domain.BudgetStatus;
import com.cloudguard.cloudguard.budget.service.BudgetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
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
    void 월_예산_상태_조회 () throws Exception {
        YearMonth yearMonth = YearMonth.of(2026,8);
        BigDecimal monthlyLimit = BigDecimal.valueOf(1000);

        given(budgetService.determineMonthlyStatus(
                yearMonth,
                monthlyLimit
        )).willReturn(BudgetStatus.CAUTION);

        mockMvc.perform(get("/api/budgets/status")
                .param("yearMonth","2026-08")
                .param("monthlyLimit","1000"))
                .andExpect(status().isOk())
                .andExpect(content().json("\"CAUTION\""));
        verify(budgetService).determineMonthlyStatus(
                yearMonth,
                monthlyLimit
        );
    }

}