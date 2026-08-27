package com.cloudguard.cloudguard.cost.aws.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.costexplorer.model.*;

import java.time.LocalDate;

@Service
public class AwsCostExplorerService {

    private final CostExplorerClient costExplorerClient;

    public AwsCostExplorerService(CostExplorerClient costExplorerClient) {
        this.costExplorerClient = costExplorerClient;
    }

    public GetCostAndUsageResponse getServiceCosts(
            LocalDate startDate,
            LocalDate endDate
    ) {
        DateInterval dateInterval = DateInterval.builder()
                .start(startDate.toString())
                .end(endDate.toString())
                .build();

        GroupDefinition groupDefinition = GroupDefinition.builder()
                .type(GroupDefinitionType.DIMENSION)
                .key("SERVICE")
                .build();

        GetCostAndUsageRequest request = GetCostAndUsageRequest.builder()
                .timePeriod(dateInterval)
                .granularity(Granularity.MONTHLY)
                .metrics("UnblendedCost")
                .groupBy(groupDefinition)
                .build();

        return costExplorerClient.getCostAndUsage(request);
    }
}
