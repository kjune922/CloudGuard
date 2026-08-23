package com.cloudguard.cloudguard.cost.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

public class MonthlyCostTest {

    @Test
    void 같은_월의_비용을_합산() {
        CostRecord ec2 = new CostRecord(
                CloudService.EC2,
                new BigDecimal("20"),
                LocalDate.of(2026,8,1)
        );

        CostRecord rds = new CostRecord(
                CloudService.RDS,
                new BigDecimal("30"),
                LocalDate.of(2026,8,6)
        );

        CostRecord s3 = new CostRecord(
                CloudService.S3,
                new BigDecimal("100"),
                LocalDate.of(2026,7,31)
        );

        MonthlyCost monthlyCost = new MonthlyCost(List.of(ec2,rds,s3));

        BigDecimal total = monthlyCost.calculateTotal(YearMonth.of(2026,8));

        Assertions.assertThat(total).isEqualTo(new BigDecimal("50"));
    }

    @Test
    void 같은_월의_비용을_서비스별로_합산() {
        CostRecord marchEc2First = new CostRecord(
                CloudService.EC2,
                BigDecimal.valueOf(3000),
                LocalDate.of(2027,3,1)
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

        CostRecord aprilS3 = new CostRecord(
                CloudService.S3,
                BigDecimal.valueOf(9000),
                LocalDate.of(2027, 4, 1)
        );

        MonthlyCost monthlyCost = new MonthlyCost(
                List.of(
                        marchEc2First,
                        marchEc2Second,
                        marchRds,
                        aprilS3
                )
        );

        // march는 3월
        Map<CloudService,BigDecimal> totals =
                monthlyCost.calculateTotalsByService(YearMonth.of(2027,3));

        Assertions.assertThat(totals).hasSize(3); // enum서비스 초기화 검증 -> 초기화시 EC2, RDS, S3 3개임
        Assertions.assertThat(totals.get(CloudService.EC2))
                .isEqualByComparingTo(BigDecimal.valueOf(4000));
        Assertions.assertThat(totals.get(CloudService.RDS))
                .isEqualByComparingTo(BigDecimal.valueOf(2000));
        Assertions.assertThat(totals.get(CloudService.S3))
                .isEqualByComparingTo(BigDecimal.ZERO);
    }
}
