package com.cloudguard.cloudguard.cost.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

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
}
