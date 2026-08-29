package com.cloudguard.cloudguard.cost.aws.service;

import com.cloudguard.cloudguard.cost.aws.dto.AwsServiceCost;
import com.cloudguard.cloudguard.cost.aws.mapper.AwsServiceNameMapper;
import com.cloudguard.cloudguard.cost.domain.CloudService;
import com.cloudguard.cloudguard.cost.service.CostService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

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

        for (AwsServiceCost awsServiceCost : awsServiceCosts) {
            CloudService cloudService = mapper.toCloudService(awsServiceCost.getServiceName());

            costService.addServiceCost(cloudService, awsServiceCost.getAmount(), startDate);
        }
    }
}
