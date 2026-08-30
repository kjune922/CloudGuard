package com.cloudguard.cloudguard.cost.aws.service;

import com.cloudguard.cloudguard.cost.aws.dto.AwsServiceCost;
import com.cloudguard.cloudguard.cost.aws.mapper.AwsServiceNameMapper;
import com.cloudguard.cloudguard.cost.domain.CloudService;
import com.cloudguard.cloudguard.cost.service.CostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Transactional
@Service
public class AwsCostImportService {

    private final AwsServiceNameMapper mapper;
    private final AwsCostExplorerService awsCostExplorerService;
    private final CostService costService;

    public AwsCostImportService(AwsServiceNameMapper mapper, AwsCostExplorerService awsCostExplorerService, CostService costService) {
        this.mapper = mapper;
        this.awsCostExplorerService = awsCostExplorerService;
        this.costService = costService;
    }

    public void importCosts(LocalDate startDate, LocalDate endDate){
        List<AwsServiceCost> awsServiceCosts = awsCostExplorerService.getServiceCosts(
                startDate,
                endDate
        );

        Map<CloudService, BigDecimal> serviceTotals = new EnumMap<>(CloudService.class);

        // CloudService가 같은 Service끼리의 비용 합산
        for (AwsServiceCost awsServiceCost : awsServiceCosts) {
            CloudService cloudService = mapper.toCloudService(awsServiceCost.getServiceName());

            serviceTotals.merge(cloudService, awsServiceCost.getAmount(), BigDecimal::add);
        }
        for (Map.Entry<CloudService, BigDecimal> entry : serviceTotals.entrySet()) {
            costService.saveOrUpdateAwsCost(entry.getKey(), entry.getValue(), startDate);
        }
    }
}
