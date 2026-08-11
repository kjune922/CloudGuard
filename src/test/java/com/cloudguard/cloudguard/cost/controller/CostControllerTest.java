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
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
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
}