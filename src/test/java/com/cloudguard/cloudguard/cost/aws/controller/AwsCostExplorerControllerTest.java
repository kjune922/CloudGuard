package com.cloudguard.cloudguard.cost.aws.controller;

import com.cloudguard.cloudguard.cost.aws.dto.AwsServiceCost;
import com.cloudguard.cloudguard.cost.aws.service.AwsCostExplorerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(AwsCostExplorerController.class)
class AwsCostExplorerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AwsCostExplorerService awsCostExplorerService;

    @Test
    void AWS_서비스별_비용_조회() throws Exception {
        LocalDate startDate = LocalDate.of(2026,8,1);
        LocalDate endDate = LocalDate.of(2026,8,30);

        List<AwsServiceCost> serviceCosts = List.of(new AwsServiceCost(
                ("Amazon Simple Storage Service"),
                new BigDecimal("0.0000000488"),
                "USD"
        ));

        given(awsCostExplorerService.getServiceCosts(
                startDate,
                endDate
        )).willReturn(serviceCosts);

        mockMvc.perform(get("/api/aws/costs")
                .param("startDate", "2026-08-01")
                .param("endDate", "2026-08-30"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].serviceName")
                        .value("Amazon Simple Storage Service"))
                .andExpect(jsonPath("$[0].amount")
                        .value(0.0000000488))
                .andExpect(jsonPath("$[0].unit")
                        .value("USD"));

        verify(awsCostExplorerService).getServiceCosts(
                startDate,
                endDate
        );
    }

}