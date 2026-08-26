package com.cloudguard.cloudguard.cost.controller;

import com.cloudguard.cloudguard.cost.domain.CloudService;
import com.cloudguard.cloudguard.cost.domain.CostRecord;
import com.cloudguard.cloudguard.cost.service.CostService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.stream.Stream;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
                        .value(4000));

        verify(costService).calculateMonthlyCostByService(yearMonth,service);

    }

    @Test
    void 월별_전체_서비스_비용_상세조회() throws Exception {
        YearMonth yearMonth = YearMonth.of(2027,3);
        Map<CloudService, BigDecimal> serviceCosts = Map.of(
                CloudService.EC2, BigDecimal.valueOf(4000),
                CloudService.RDS, BigDecimal.valueOf(2000),
                CloudService.S3, BigDecimal.ZERO
        );

        given(costService.calculateMonthlyCostBreakdown(yearMonth))
                .willReturn(serviceCosts);

        mockMvc.perform(get("/api/costs/monthly/breakdown")
                .param("yearMonth","2027-03"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.yearMonth")
                        .value("2027-03"))
                .andExpect(jsonPath("$.totalCost")
                        .value(6000))
                .andExpect(jsonPath("$.serviceCosts.EC2")
                        .value(4000))
                .andExpect(jsonPath("$.serviceCosts.RDS")
                        .value(2000))
                .andExpect(jsonPath("$.serviceCosts.S3")
                        .value(0));

        verify(costService).calculateMonthlyCostBreakdown(yearMonth);
    }

    @Test
    void 음수_비용을_등록하면_400() throws Exception{
        mockMvc.perform(post("/api/costs/add-cost")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "cloudService" : "EC2",
                            "cost" : -1000,
                            "usageDate" : "2026-08-26"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value("비용은 음수일 수 없습니다."))
                .andExpect(jsonPath("$.path")
                        .value("/api/costs/add-cost"));

        verifyNoInteractions(costService);
    }

    @ParameterizedTest
    @MethodSource("invalidCostCreateRequests")
    void 비용등록시_필수값이_누락되면_400(String requestBody, String expectedMessage) throws Exception {
        mockMvc.perform(post("/api/costs/add-cost")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code")
                        .value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message")
                        .value(expectedMessage))
                .andExpect(jsonPath("$.path")
                        .value("/api/costs/add-cost"));

        verifyNoInteractions(costService);
    }
    static Stream<Arguments> invalidCostCreateRequests(){
        return Stream.of(
                Arguments.of(
                        """
                                {
                                    "cost" : 1000,
                                    "usageDate" : "2026-08-26"
                                }
                                """,
                        "클라우드 서비스는 필수입니다."
                ),
                Arguments.of(
                        """
                                {
                                    "cloudService" : "EC2",
                                    "usageDate" : "2026-08-26"
                                }
                                """,
                        "비용은 필수입니다."

                ),
                Arguments.of(
                        """
                                {
                                    "cloudService" : "EC2",
                                    "cost" : 1000
                                }
                                """,
                        "비용 발생일은 필수입니다."
                )
        );
    }

    @ParameterizedTest
    @MethodSource("invalidCostRequestFormats")
    void 비용등록시_요청형식이_잘못되면_400(String requestBody) throws Exception{
        mockMvc.perform(post("/api/costs/add-cost")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code")
                        .value("INVALID_REQUEST_FORMAT"))
                .andExpect(jsonPath("$.message")
                        .value("요청 형식이 올바르지 않습니다."))
                .andExpect(jsonPath("$.path")
                        .value("/api/costs/add-cost"));

        verifyNoInteractions(costService);
    }
    static Stream<Arguments> invalidCostRequestFormats(){
        return Stream.of(
                Arguments.of(
                        """
                                {
                                    "cloudService" : "LAMBDA",
                                    "cost" : 1000,
                                    "usageData" : "2026-08-26"
                                }
                                """
                ),
                Arguments.of(
                        """
                            {
                                "cloudService" : "EC2",
                                "cost" : 1000,
                                "usageDate" : "2026-13-34"
                            }
                            """
                )
        );
    }
}