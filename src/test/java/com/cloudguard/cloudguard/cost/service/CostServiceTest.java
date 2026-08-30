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
import java.util.Map;

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
    @Test
    void 비용기록이_없는_월의_총비용은_0() {
        YearMonth yearMonth = YearMonth.of(2050, 10);

        BigDecimal totalCost =
                costService.calculateMonthlyCost(yearMonth);

        Assertions.assertThat(totalCost)
                .isEqualByComparingTo(BigDecimal.ZERO);
    }
    @Test
    void 특정_월의_특정_서비스_비용을_합산() {
        CostRecord marchEc2First = new CostRecord(
                CloudService.EC2,
                BigDecimal.valueOf(3000),
                LocalDate.of(2027, 3, 1)
        );

        CostRecord marchEc2Second = new CostRecord(
                CloudService.EC2,
                BigDecimal.valueOf(1000),
                LocalDate.of(2027, 3, 31)
        );

        CostRecord marchRds = new CostRecord(
                CloudService.RDS,
                BigDecimal.valueOf(2000),
                LocalDate.of(2027, 3, 15)
        );

        CostRecord aprilEc2 = new CostRecord(
                CloudService.EC2,
                BigDecimal.valueOf(9000),
                LocalDate.of(2027, 4, 1)
        );

        costRecordRepository.save(marchEc2First);
        costRecordRepository.save(marchEc2Second);
        costRecordRepository.save(marchRds);
        costRecordRepository.save(aprilEc2);

        BigDecimal totalCost =
                costService.calculateMonthlyCostByService(
                        YearMonth.of(2027, 3),
                        CloudService.EC2
                );

        Assertions.assertThat(totalCost)
                .isEqualByComparingTo(BigDecimal.valueOf(4000));
    }

    @Test
    void 월별_전체_서비스_비용을_DB에서_조회하고_합산() {
        CostRecord marchEc2First = new CostRecord(
                CloudService.EC2,
                BigDecimal.valueOf(3000),
                LocalDate.of(2027,3,1)
        );
        CostRecord marchEc2Second = new CostRecord(
                CloudService.EC2,
                BigDecimal.valueOf(1000),
                LocalDate.of(2027,3,31)
        );
        CostRecord marchRds = new CostRecord(
                CloudService.RDS,
                BigDecimal.valueOf(2000),
                LocalDate.of(2027,3,15)
        );
        CostRecord aprilS3 = new CostRecord(
                CloudService.S3,
                BigDecimal.valueOf(9000),
                LocalDate.of(2027,4,1)
        );

        costRecordRepository.save(marchEc2First);
        costRecordRepository.save(marchEc2Second);
        costRecordRepository.save(marchRds);
        costRecordRepository.save(aprilS3);

        Map<CloudService, BigDecimal> totals = costService.calculateMonthlyCostBreakdown(
                YearMonth.of(2027,3)
        );

        Assertions.assertThat(totals).hasSize(4);
        Assertions.assertThat(totals.get(CloudService.EC2))
                .isEqualByComparingTo(BigDecimal.valueOf(4000));
        Assertions.assertThat(totals.get(CloudService.RDS))
                .isEqualByComparingTo(BigDecimal.valueOf(2000));
        Assertions.assertThat(totals.get(CloudService.S3))
                .isEqualByComparingTo(BigDecimal.ZERO);
        Assertions.assertThat(totals.get(CloudService.OTHER))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }
}