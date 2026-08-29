package com.cloudguard.cloudguard.cost.aws.mapper;

import com.cloudguard.cloudguard.cost.domain.CloudService;

public class AwsServiceNameMapper {

    public CloudService toCloudService(String awsServiceName){
        return switch (awsServiceName) {
            case "Amazon Elastic Compute Cloud - Compute",
                 "EC2 - OTHER" -> CloudService.EC2;

            case "Amazon Relational Database Service" ->
                    CloudService.RDS;

            case "Amazon Simple Storage Service" ->
                    CloudService.S3;

            default -> CloudService.OTHER;
        };
    }
}
