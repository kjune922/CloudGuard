package com.cloudguard.cloudguard.cost.aws.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;

/**
 * 외부 라이브러리 객체도 내가 @Bean으로 등록하면 Spring이 관리하고 주입 할 수 있음.
 * 김영한 Spring 핵심 원리에서 배운 수동 빈 등록 부분 참고.
 * 여기서 리전을 US_EAST_1로 지정한 이유: AWS Cost Explorer API의 엔드포인트가 이 리전에 있음
 */

@Configuration
public class AwsCostExplorerConfig {

    @Bean
    public CostExplorerClient costExplorerClient() {
        return CostExplorerClient.builder()
                .region(Region.US_EAST_1)
                .build();
    }
}
