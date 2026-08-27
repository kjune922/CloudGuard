package com.cloudguard.cloudguard.cost.aws.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;

@Configuration
public class AwsCostExplorerConfig {

    @Bean
    public CostExplorerClient costExplorerClient() {
        return CostExplorerClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }
}
