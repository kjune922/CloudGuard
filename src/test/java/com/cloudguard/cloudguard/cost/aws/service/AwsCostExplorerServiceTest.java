package com.cloudguard.cloudguard.cost.aws.service;

import com.cloudguard.cloudguard.cost.aws.dto.AwsServiceCost;
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
}