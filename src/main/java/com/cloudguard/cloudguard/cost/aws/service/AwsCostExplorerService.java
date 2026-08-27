package com.cloudguard.cloudguard.cost.aws.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;

@Service
public class AwsCostExplorerService {

    private final CostExplorerClient costExplorerClient;

    public AwsCostExplorerService(CostExplorerClient costExplorerClient) {
        this.costExplorerClient = costExplorerClient;
    }
}
