package com.cloudguard.cloudguard.cost.service;

import com.cloudguard.cloudguard.cost.domain.CloudService;
import com.cloudguard.cloudguard.cost.domain.CostRecord;
import com.cloudguard.cloudguard.cost.repository.CostRecordRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

@Transactional
@SpringBootTest
class CostServiceTest {

    @Autowired
    CostService costService;

    @Autowired
    CostRecordRepository costRecordRepository;

    @Test
    void 월_총비용을_제대로_반환하는가() {

        CostRecord costRecord1 = new CostRecord(
                CloudService.EC2,
                BigDecimal.valueOf(100),
                LocalDate.of(2026,8,6)
        );
        CostRecord costRecord2 = new CostRecord(
                CloudService.S3,
                BigDecimal.valueOf(500),
                LocalDate.of(2026,8,31)
        );
        CostRecord costRecord3 = new CostRecord(
                CloudService.RDS,
                BigDecimal.valueOf(300),
                LocalDate.of(2026,7,7)
        );

        costRecordRepository.save(costRecord1);
        costRecordRepository.save(costRecord2);
        costRecordRepository.save(costRecord3);

        YearMonth testDate = YearMonth.of(2026,8);

        Assertions.assertThat(costService.calculateMonthlyCost(testDate))
                .isEqualByComparingTo(BigDecimal.valueOf(600));
    }
}