package com.cloudguard.cloudguard.cost.aws.service;

import com.cloudguard.cloudguard.cost.aws.dto.AwsServiceCost;
import com.cloudguard.cloudguard.cost.aws.exception.InvalidCostPeriodException;
import com.cloudguard.cloudguard.cost.dto.AwsDailyServiceCost;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.costexplorer.CostExplorerClient;
import software.amazon.awssdk.services.costexplorer.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AwsCostExplorerService {

    private final CostExplorerClient costExplorerClient;

//    @Autowired
//    public AwsCostExplorerService(CostExplorerClient costExplorerClient) {
//        this.costExplorerClient = costExplorerClient;
//    }

    // 일별 비용 조회
    public List<AwsDailyServiceCost> getDailyServiceCosts(
            LocalDate startDate, LocalDate endDate) {

        validatePeriod(startDate,endDate);

        DateInterval dateInterval = DateInterval.builder()
                .start(startDate.toString())
                .end(endDate.toString())
                .build();

        GroupDefinition groupDefinition = GroupDefinition.builder()
                .type(GroupDefinitionType.DIMENSION)
                .key("SERVICE")
                .build();

        // "이 기간의 UnblendedCost를 일별, 서비스별로 조회해 줘"
        GetCostAndUsageRequest request = GetCostAndUsageRequest.builder()
                .timePeriod(dateInterval) // 앞에서 만든 조회 기간
                .granularity(Granularity.DAILY) // 일별 집계
                .metrics("UnblendedCost") // 조회할 비용 지표
                .groupBy(groupDefinition) // 서비스별로 분류
                .build();

        List<AwsDailyServiceCost> allServiceCosts = new ArrayList<>();

        while (true) {
            // 실제 AWS 호출
            /**
             * 1. 무엇을 요청하는가?
             * request에 기간, 일별 집계, 비용 지표, 서비스별 분류 조건을 넣는다.
             *
             * 2. 어떤 일을 하는가?
             * AWS SDK의 Client가 해당 조건으로 AWS Cost Explorer를 호출한다.
             *
             * 3. 무엇을 받는가?
             * AWS 응답을 GetCostAndUsageResponse 객체로 받아 response 변수가 가리킨다.
             * 응답에는 조회 결과와 다음 페이지 토큰 등이 들어 있다.
             */
            GetCostAndUsageResponse response =
                    costExplorerClient.getCostAndUsage(request);

            // 현재 페이지 결과를 누적
            allServiceCosts.addAll(
                    convertToDailyServiceCosts(response)
            );

            String nextPageToken = response.nextPageToken();

            // 다음 페이지가 없으면 종료
            if (nextPageToken == null || nextPageToken.isBlank()) {
                break;
            }

            // 기존 조회 조건을 유지하고 다음 페이지 토큰만 설정
            request = request.toBuilder()
                    .nextPageToken(nextPageToken)
                    .build();
        }

        return allServiceCosts;
    }

    public List<AwsServiceCost> getServiceCosts(
            LocalDate startDate,
            LocalDate endDate
    ) {
        validatePeriod(startDate,endDate);

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

        List<AwsServiceCost> allServiceCosts = new ArrayList<>();

        while (true) {
            GetCostAndUsageResponse response =
                    costExplorerClient.getCostAndUsage(request);

            allServiceCosts.addAll(convertToServiceCosts(response));

            String nextPageToken = response.nextPageToken();

            if (nextPageToken == null || nextPageToken.isBlank()) {
                break;
            }

            request = request.toBuilder()
                    .nextPageToken(nextPageToken)
                    .build();
        }

        return allServiceCosts;
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

                // unit은 금액의 단위
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

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null){
            throw  new InvalidCostPeriodException("시작과 종료일은 필수입니다.");
        }

        if(!startDate.isBefore(endDate)){
            throw new InvalidCostPeriodException("시작일은 종료일보다 빨라야 합니다.");
        }
    }
}
