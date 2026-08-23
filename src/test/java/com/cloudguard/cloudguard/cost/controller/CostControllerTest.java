package com.cloudguard.cloudguard.cost.controller;

import com.cloudguard.cloudguard.cost.domain.CloudService;
import com.cloudguard.cloudguard.cost.domain.CostRecord;
import com.cloudguard.cloudguard.cost.service.CostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(CostController.class)
class CostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CostService costService;

    @Test
    void 비용_등록() throws Exception {
        CostRecord savedRecord = new CostRecord(
                CloudService.EC2,
                BigDecimal.valueOf(800),
                LocalDate.of(2026,8,1));

        given(costService.addServiceCost(
                CloudService.EC2,
                BigDecimal.valueOf(800),
                LocalDate.of(2026,8,1)
        )).willReturn(savedRecord);

        mockMvc.perform(post("/api/costs/add-cost")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""    
                        {
                            "cloudService" : "EC2",
                            "cost": 800,
                            "usageDate": "2026-08-01"
                       
                        }
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.service").value("EC2"))
                .andExpect(jsonPath("$.cost").value(800))
                .andExpect(jsonPath("$.usageDate").value("2026-08-01"));

        verify(costService).addServiceCost(
                CloudService.EC2,
                BigDecimal.valueOf(800),
                LocalDate.of(2026,8,1)
        );
    }

    @Test
    void 월_누적_비용_조회() throws Exception {
        YearMonth yearMonth = YearMonth.of(2026,8);
        BigDecimal totalCost = BigDecimal.valueOf(5000);

        given(costService.calculateMonthlyCost(yearMonth))
                .willReturn(totalCost);

        mockMvc.perform(get("/api/costs/monthly")
                .param("yearMonth","2026-08"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yearMonth")
                        .value("2026-08"))
                .andExpect(jsonPath("$.totalCost")
                        .value(5000));

        verify(costService).calculateMonthlyCost(yearMonth);
    }

    @Test
    void 서비스별_월_누적_비용조회() throws Exception{
        YearMonth yearMonth = YearMonth.of(2027,3);
        CloudService service = CloudService.EC2;
        BigDecimal totalCost = BigDecimal.valueOf(4000);

        given(costService.calculateMonthlyCostByService(yearMonth,service))
                .willReturn(totalCost);

        mockMvc.perform(get("/api/costs/monthly/by-service")
                .param("yearMonth","2027-03")
                .param("service","EC2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yearMonth")
                        .value("2027-03"))
                .andExpect(jsonPath("$.service")
                        .value("EC2"))
                .andExpect(jsonPath("$.totalCost")
                        .value("4000"));

        verify(costService).calculateMonthlyCostByService(yearMonth,service);

    }
}