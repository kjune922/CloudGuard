package com.cloudguard.cloudguard.cost.aws.service;

import com.cloudguard.cloudguard.cost.aws.mapper.AwsServiceNameMapper;
import com.cloudguard.cloudguard.cost.domain.CloudService;
import com.cloudguard.cloudguard.cost.dto.AwsDailyServiceCost;
import com.cloudguard.cloudguard.cost.service.CostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

class AwsCostImportServiceTest {

    private AwsCostImportService awsCostImportService;
    private CostService costService;
    private AwsCostExplorerService awsCostExplorerService;
    private AwsServiceNameMapper awsServiceNameMapper;

    @BeforeEach
    void setUp() {
        awsCostExplorerService = mock(AwsCostExplorerService.class);
        awsServiceNameMapper = mock(AwsServiceNameMapper.class);
        costService = mock(CostService.class);
        awsCostImportService = new AwsCostImportService(
                awsServiceNameMapper,
                awsCostExplorerService,
                costService);
    }

    @Test
    void AWS_비용을_CloudGuard_비용으로_저장() {
        LocalDate startDate = LocalDate.of(2026,8,1);
        LocalDate endDate = LocalDate.of(2026,8,30);

        AwsDailyServiceCost awsServiceCost = new AwsDailyServiceCost(
                "Amazon Simple Storage Service",
                new BigDecimal("0.0000000488"),
                "USD",
                startDate
        );

        given(awsCostExplorerService.getDailyServiceCosts(
                startDate,endDate
        )).willReturn(List.of(awsServiceCost));

        given(awsServiceNameMapper.toCloudService(
                "Amazon Simple Storage Service"
        )).willReturn(CloudService.S3);

        awsCostImportService.importCosts(startDate,endDate);

        verify(awsCostExplorerService).getDailyServiceCosts(startDate,endDate);
        verify(awsServiceNameMapper).toCloudService("Amazon Simple Storage Service");
        verify(costService).saveOrUpdateAwsCost(
                CloudService.S3,
                new BigDecimal("0.0000000488"),
                startDate);
    }

    @Test
    void 같은_CloudService로_변환되는_AWS_비용은_합산해서_저장 () {
        LocalDate startDate = LocalDate.of(2026,8,1);
        LocalDate endDate = LocalDate.of(2026,8,31);

        AwsDailyServiceCost ec2Compute = new AwsDailyServiceCost(
                "Amazon Elastic Compute Cloud - Compute",
                new BigDecimal("10"),
                "USD",
                startDate
        );

        AwsDailyServiceCost ec2Other = new AwsDailyServiceCost(
                "EC2 - Other",
                new BigDecimal("5"),
                "USD",
                startDate
        );

        given(awsCostExplorerService.getDailyServiceCosts(
                startDate,endDate
        )).willReturn(List.of(ec2Compute,ec2Other));

        given(awsServiceNameMapper.toCloudService(
                "Amazon Elastic Compute Cloud - Compute"
        )).willReturn(CloudService.EC2);

        given(awsServiceNameMapper.toCloudService("EC2 - Other"))
                .willReturn(CloudService.EC2);

        awsCostImportService.importCosts(startDate,endDate);

        verify(costService).saveOrUpdateAwsCost(
                CloudService.EC2,
                new BigDecimal("15"),
                startDate
        );

        verifyNoMoreInteractions(costService);
    }

    @Test
    void 같은_날짜의_서비스비용만_합산해서_저장함() {
        LocalDate firstDate = LocalDate.of(2026,8,10);
        LocalDate secondDate = LocalDate.of(2026,8,11);
        LocalDate endDate = LocalDate.of(2026,8,12);

        given(awsCostExplorerService.getDailyServiceCosts(
                firstDate,
                endDate
        )).willReturn(List.of(
                new AwsDailyServiceCost(
                        "Amazon Elastic Compute Cloud - Compute",
                        new BigDecimal("10"),
                        "USD",
                        firstDate
                ),
                new AwsDailyServiceCost(
                        "EC2 - Other",
                        new BigDecimal("5"),
                        "USD",
                        firstDate
                ),
                new AwsDailyServiceCost(
                        "Amazon Elastic Compute Cloud - Compute",
                        new BigDecimal("7"),
                        "USD",
                        secondDate
                )
        ));

        given(awsServiceNameMapper.toCloudService(
                "Amazon Elastic Compute Cloud - Compute"
        )).willReturn(CloudService.EC2);

        given(awsServiceNameMapper.toCloudService(
                "EC2 - Other"
        )).willReturn(CloudService.EC2);

        awsCostImportService.importCosts(firstDate, endDate);

        verify(costService).saveOrUpdateAwsCost(
                CloudService.EC2,
                new BigDecimal("15"),
                firstDate
        );

        verify(costService).saveOrUpdateAwsCost(
                CloudService.EC2,
                new BigDecimal(7),
                secondDate
        );

        verifyNoMoreInteractions(costService);
    }

    @Test
    void USD가_아닌_비용이_섞이면_아무것도_저장하지_않는다() {
        LocalDate startDate = LocalDate.of(2026, 8, 10);
        LocalDate endDate = LocalDate.of(2026, 8, 11);

        given(awsCostExplorerService.getDailyServiceCosts(
                startDate, endDate
        )).willReturn(List.of(
                new AwsDailyServiceCost(
                        "Amazon Simple Storage Service",
                        new BigDecimal("10"),
                        "USD",
                        startDate
                ),
                new AwsDailyServiceCost(
                        "Amazon Simple Storage Service",
                        new BigDecimal("1000"),
                        "KRW",
                        startDate
                )
        ));

        given(awsServiceNameMapper.toCloudService(
                "Amazon Simple Storage Service"
        )).willReturn(CloudService.S3);

        assertThatThrownBy(() ->
                awsCostImportService.importCosts(startDate, endDate)
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("지원하지 않는 AWS 비용 통화입니다: KRW");

        verifyNoInteractions(costService);
    }

}