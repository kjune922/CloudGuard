package com.cloudguard.cloudguard.cost.aws.service;


import com.cloudguard.cloudguard.cost.aws.dto.AwsServiceCost;
import com.cloudguard.cloudguard.cost.domain.CloudService;
import com.cloudguard.cloudguard.cost.domain.CostRecord;
import com.cloudguard.cloudguard.cost.repository.CostRecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Transactional
public class AwsCostImportServiceIntegrationTest {

    @Autowired
    private AwsCostImportService awsCostImportService;

    @Autowired
    private CostRecordRepository costRecordRepository;

    @MockitoBean
    private AwsCostExplorerService awsCostExplorerService;

    @Test
    void AWS_비용을_DB에_실제로_저장() {
        LocalDate startDate = LocalDate.of(2026,8,1);
        LocalDate endDate = LocalDate.of(2026,8,31);

        given(awsCostExplorerService.getServiceCosts(startDate,endDate))
                .willReturn(List.of(new AwsServiceCost(
                        "Amazon Simple Storage Service",
                        new BigDecimal("10.5"),
                        "USD"
                )));

        awsCostImportService.importCosts(startDate,endDate);

        List<CostRecord> records = costRecordRepository.findByUsageDateBetween(startDate,endDate);
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getService()).isEqualTo(CloudService.S3);
        assertThat(records.get(0).getCost()).isEqualByComparingTo("10.5");
        assertThat(records.get(0).getUsageDate()).isEqualTo(startDate);
    }

    @Test
    void 같은_기간을_두번_저장해도_비용은_중복되지않음 () {
        LocalDate startDate = LocalDate.of(2026,8,1);
        LocalDate endDate = LocalDate.of(2026,8,31);

        given(awsCostExplorerService.getServiceCosts(startDate,endDate))
                .willReturn(List.of(new AwsServiceCost(
                        "Amazon Simple Storage Service",
                        new BigDecimal("10.5"),
                        "USD"
                )));

        awsCostImportService.importCosts(startDate,endDate);
        awsCostImportService.importCosts(startDate,endDate);

        List<CostRecord> records = costRecordRepository.findByUsageDateBetween(startDate,endDate);

        assertThat(records).hasSize(1);
        assertThat(records.get(0).getService()).isEqualTo(CloudService.S3);
        assertThat(records.get(0).getCost()).isEqualByComparingTo("10.5");
        assertThat(records.get(0).getUsageDate()).isEqualTo(startDate);
    }
}
