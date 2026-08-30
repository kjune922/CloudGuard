package com.cloudguard.cloudguard.cost.aws.service;

import com.cloudguard.cloudguard.cost.aws.dto.AwsServiceCost;
import com.cloudguard.cloudguard.cost.dto.AwsDailyServiceCost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.costexplorer.model.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AwsCostExplorerServiceTest {

    private CostExplorerClient costExplorerClient;
    private AwsCostExplorerService awsCostExplorerService;

    @BeforeEach
    void setUp() {
        costExplorerClient = mock(CostExplorerClient.class);
        awsCostExplorerService =
                new AwsCostExplorerService(costExplorerClient);
    }

    private ResultByTime createDailyResult(
            String startDate,
            String endDate,
            String amount
    ) {
        MetricValue metric = MetricValue.builder()
                .amount(amount)
                .unit("USD")
                .build();

        Group group = Group.builder()
                .keys("Amazon Simple Storage Service")
                .metrics(Map.of("UnblendedCost", metric))
                .build();

        return ResultByTime.builder()
                .timePeriod(DateInterval.builder()
                        .start(startDate)
                        .end(endDate)
                        .build())
                .groups(group)
                .build();
    }

    @Test
    void AWS_서비스별_비용을_조회하고_DTO로_변환() {
        MetricValue metric = MetricValue.builder()
                .amount("0.0000000488")
                .unit("USD")
                .build();

        Group group = Group.builder()
                .keys("Amazon Simple Storage Service")
                .metrics(Map.of("UnblendedCost", metric))
                .build();

        ResultByTime resultByTime = ResultByTime.builder()
                .groups(group)
                .build();

        GetCostAndUsageResponse response =
                GetCostAndUsageResponse.builder()
                        .resultsByTime(resultByTime)
                        .build();

        given(costExplorerClient.getCostAndUsage(
                any(GetCostAndUsageRequest.class)
        )).willReturn(response);

        List<AwsServiceCost> result =
                awsCostExplorerService.getServiceCosts(
                        LocalDate.of(2026, 8, 1),
                        LocalDate.of(2026, 8, 27)
                );

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getServiceName())
                .isEqualTo("Amazon Simple Storage Service");
        assertThat(result.get(0).getAmount())
                .isEqualByComparingTo("0.0000000488");
        assertThat(result.get(0).getUnit())
                .isEqualTo("USD");

        ArgumentCaptor<GetCostAndUsageRequest> captor =
                ArgumentCaptor.forClass(GetCostAndUsageRequest.class);

        verify(costExplorerClient)
                .getCostAndUsage(captor.capture());

        GetCostAndUsageRequest request = captor.getValue();

        assertThat(request.timePeriod().start())
                .isEqualTo("2026-08-01");
        assertThat(request.timePeriod().end())
                .isEqualTo("2026-08-27");
        assertThat(request.granularity())
                .isEqualTo(Granularity.MONTHLY);
        assertThat(request.metrics())
                .containsExactly("UnblendedCost");
        assertThat(request.groupBy().get(0).key())
                .isEqualTo("SERVICE");
    }

    @Test
    void AWS_일별_비용을_날짜와_함께_DTO로_변환 () {
        GetCostAndUsageResponse response = GetCostAndUsageResponse.builder()
                .resultsByTime(
                        createDailyResult(
                                "2026-08-10",
                                "2026-08-11",
                                "3"
                        ),
                        createDailyResult(
                                "2026-08-11",
                                "2026-08-12",
                                "5"
                        )
                ).build();

        given(costExplorerClient.getCostAndUsage(
                any(GetCostAndUsageRequest.class)
        )).willReturn(response);

        List<AwsDailyServiceCost> result = awsCostExplorerService.getDailyServiceCosts(
                LocalDate.of(2026,8,10),
                LocalDate.of(2026,8,12)
        );

        assertThat(result).hasSize(2);

        assertThat(result).extracting(AwsDailyServiceCost::getUsageDate)
                .containsExactly(
                        LocalDate.of(2026,8,10),
                        LocalDate.of(2026,8,11)
                );
        assertThat(result)
                .extracting(AwsDailyServiceCost::getServiceName)
                .containsOnly("Amazon Simple Storage Service");

        assertThat(result)
                .extracting(AwsDailyServiceCost::getUnit)
                .containsOnly("USD");

        assertThat(result.get(0).getAmount())
                .isEqualByComparingTo("3");
        assertThat(result.get(1).getAmount())
                .isEqualByComparingTo("5");

        ArgumentCaptor<GetCostAndUsageRequest> captor =
                ArgumentCaptor.forClass(GetCostAndUsageRequest.class);

        verify(costExplorerClient).getCostAndUsage(captor.capture());

        GetCostAndUsageRequest request = captor.getValue();

        assertThat(request.granularity())
                .isEqualTo(Granularity.DAILY);
        assertThat(request.timePeriod().start())
                .isEqualTo("2026-08-10");
        assertThat(request.timePeriod().end())
                .isEqualTo("2026-08-12");
    }
}