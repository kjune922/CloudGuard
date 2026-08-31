package com.cloudguard.cloudguard.cost.aws.controller;

import com.cloudguard.cloudguard.cost.aws.exception.InvalidCostPeriodException;
import com.cloudguard.cloudguard.cost.aws.service.AwsCostImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@WebMvcTest(AwsCostImportController.class)
class AwsCostImportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AwsCostImportService awsCostImportService;

    @Test
    void AWS_비용_수집을_요청하면_204_반환() throws Exception {
        mockMvc.perform(post("/api/aws/costs/import")
                .param("startDate", "2026-08-01")
                .param("endDate", "2026-08-30"))
                .andExpect(status().isNoContent());

        verify(awsCostImportService).importCosts(
                LocalDate.of(2026,8,1),
                LocalDate.of(2026,8,30)
        );
    }

    @Test
    void 잘못된_수집_기간이면_400과_오류_JSON을_반환한다()
            throws Exception {
        LocalDate startDate = LocalDate.of(2026, 8, 12);
        LocalDate endDate = LocalDate.of(2026, 8, 10);

        doThrow(new InvalidCostPeriodException(
                "시작일은 종료일보다 빨라야 합니다."
        )).when(awsCostImportService).importCosts(startDate, endDate);

        mockMvc.perform(post("/api/aws/costs/import")
                        .param("startDate", "2026-08-12")
                        .param("endDate", "2026-08-10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code")
                        .value("INVALID_COST_PERIOD"))
                .andExpect(jsonPath("$.message")
                        .value("시작일은 종료일보다 빨라야 합니다."))
                .andExpect(jsonPath("$.path")
                        .value("/api/aws/costs/import"));
    }

}