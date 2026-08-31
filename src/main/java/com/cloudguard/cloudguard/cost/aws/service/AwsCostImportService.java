package com.cloudguard.cloudguard.cost.aws.service;

import com.cloudguard.cloudguard.cost.aws.mapper.AwsServiceNameMapper;
import com.cloudguard.cloudguard.cost.domain.CloudService;
import com.cloudguard.cloudguard.cost.dto.AwsDailyServiceCost;
import com.cloudguard.cloudguard.cost.service.CostService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
        List<AwsDailyServiceCost> dailyServiceCosts = awsCostExplorerService.getDailyServiceCosts(
                startDate,
                endDate
        );

        // TreeMap을 쓴 이유는 날짜순서대로 할려고
        Map<LocalDate, Map<CloudService, BigDecimal>> dailyTotals = new TreeMap<>();

        // 날짜별 - 서비스별 합산
        for (AwsDailyServiceCost dailyCost : dailyServiceCosts) {

            validateCurrency(dailyCost.getUnit());
            LocalDate usageDate = dailyCost.getUsageDate();

            CloudService cloudService = mapper.toCloudService(dailyCost.getServiceName());

            Map<CloudService, BigDecimal> serviceTotals = dailyTotals.computeIfAbsent(
                    usageDate,
                    date -> new EnumMap<>(CloudService.class)
            );

            serviceTotals.merge(
                    cloudService,
                    dailyCost.getAmount(),
                    BigDecimal::add
            );
        }

        // 합산 결과를 해당 날짜로 저장 하고 갱신함
        for (Map.Entry<LocalDate, Map<CloudService, BigDecimal>> dailyEntry : dailyTotals.entrySet()) {
            LocalDate usageDate = dailyEntry.getKey();

            for (Map.Entry<CloudService, BigDecimal> serviceEntry : dailyEntry.getValue().entrySet()) {

                costService.saveOrUpdateAwsCost(
                        serviceEntry.getKey(),
                        serviceEntry.getValue(),
                        usageDate
                );
            }
        }
    }
    private void validateCurrency(String unit) {
        if (!"USD".equals(unit)) {
            throw new IllegalStateException(
                    "지원하지 않는 AWS 비용 통화입니다: " + unit
            );
        }
    }
}
