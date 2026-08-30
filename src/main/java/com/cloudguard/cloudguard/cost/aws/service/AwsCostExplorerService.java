package com.cloudguard.cloudguard.cost.aws.service;

import com.cloudguard.cloudguard.cost.aws.dto.AwsServiceCost;
import com.cloudguard.cloudguard.cost.dto.AwsDailyServiceCost;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.costexplorer.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class AwsCostExplorerService {

    private final CostExplorerClient costExplorerClient;

    public AwsCostExplorerService(CostExplorerClient costExplorerClient) {
        this.costExplorerClient = costExplorerClient;
    }

    // 일별 비용 조회
    public List<AwsDailyServiceCost> getDailyServiceCosts(
            LocalDate startDate, LocalDate endDate) {
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
                .granularity(Granularity.DAILY)
                .metrics("UnblendedCost")
                .groupBy(groupDefinition)
                .build();

        GetCostAndUsageResponse response = costExplorerClient.getCostAndUsage(request);

        return convertToDailyServiceCosts(response);
    }

    public List<AwsServiceCost> getServiceCosts(
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

        GetCostAndUsageResponse response = costExplorerClient.getCostAndUsage(request);

        return convertToServiceCosts(response);
    }

    private List<AwsServiceCost> convertToServiceCosts(GetCostAndUsageResponse response) {

        List<AwsServiceCost> serviceCosts = new ArrayList<>();

        for (ResultByTime resultByTime : response.resultsByTime()) {
            for (Group group : resultByTime.groups()) {
                String serviceName = group.keys().get(0);

                MetricValue metric = group.metrics().get("UnblendedCost");

                BigDecimal amount = new BigDecimal(metric.amount());

                String unit = metric.unit();

                serviceCosts.add(new AwsServiceCost(serviceName, amount, unit));
            }
        }
        return serviceCosts;
    }

    private List<AwsDailyServiceCost> convertToDailyServiceCosts(GetCostAndUsageResponse response){

        List<AwsDailyServiceCost> serviceCosts = new ArrayList<>();

        for (ResultByTime resultByTime : response.resultsByTime()) {
            LocalDate usageDate = LocalDate.parse(resultByTime.timePeriod().start());

            for (Group group : resultByTime.groups()) {
                String serviceName = group.keys().get(0);

                MetricValue metric = group.metrics().get("UnblendedCost");

                BigDecimal amount = new BigDecimal(metric.amount());
                String unit = metric.unit();

                serviceCosts.add(new AwsDailyServiceCost(
                        serviceName,
                        amount,
                        unit,
                        usageDate
                ));
            }
        }
        return serviceCosts;
    }
}
