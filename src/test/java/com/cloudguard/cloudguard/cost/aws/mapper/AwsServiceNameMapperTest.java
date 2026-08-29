package com.cloudguard.cloudguard.cost.aws.mapper;

import com.cloudguard.cloudguard.cost.domain.CloudService;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class AwsServiceNameMapperTest {

    private final AwsServiceNameMapper mapper = new AwsServiceNameMapper();

    @ParameterizedTest
    @CsvSource({
            "'Amazon Elastic Compute Cloud - Compute', EC2",
            "'EC2 - Other', EC2",
            "'Amazon Relational Database Service', RDS",
            "'Amazon Simple Storage Service', S3",
            "'AWS Glue', OTHER",
            "'AWS Key Management Service', OTHER"
    })
    void AWS_서비스명_CloudGuard_서비스로_전환 (String awsServiceName, CloudService expected) {
        CloudService result = mapper.toCloudService(awsServiceName);

        assertThat(result).isEqualTo(expected);
    }

}