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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

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
        verify(costService).addServiceCost(
                CloudService.S3,
                new BigDecimal("0.0000000488"),
                startDate);
    }

}