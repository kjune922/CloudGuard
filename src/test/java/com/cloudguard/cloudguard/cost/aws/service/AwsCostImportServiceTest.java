package com.cloudguard.cloudguard.cost.aws.service;

import com.cloudguard.cloudguard.cost.aws.dto.AwsServiceCost;
import com.cloudguard.cloudguard.cost.aws.mapper.AwsServiceNameMapper;
import com.cloudguard.cloudguard.cost.domain.CloudService;
import com.cloudguard.cloudguard.cost.service.CostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

        AwsServiceCost awsServiceCost = new AwsServiceCost(
                "Amazon Simple Storage Service",
                new BigDecimal("0.0000000488"),
                "USD"
        );

        given(awsCostExplorerService.getServiceCosts(
                startDate,endDate
        )).willReturn(List.of(awsServiceCost));

        given(awsServiceNameMapper.toCloudService(
                "Amazon Simple Storage Service"
        )).willReturn(CloudService.S3);

        awsCostImportService.importCosts(startDate,endDate);

        verify(awsCostExplorerService).getServiceCosts(startDate,endDate);
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

        AwsServiceCost ec2Compute = new AwsServiceCost(
                "Amazon Elastic Compute Cloud - Compute",
                new BigDecimal("10"),
                "USD");

        AwsServiceCost ec2Other = new AwsServiceCost(
                "EC2 - Other",
                new BigDecimal("5"),
                "USD");

        given(awsCostExplorerService.getServiceCosts(
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

}