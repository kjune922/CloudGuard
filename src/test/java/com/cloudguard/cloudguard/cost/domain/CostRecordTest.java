package com.cloudguard.cloudguard.cost.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

public class CostRecordTest {

    @Test
    void 비용_기록_생성() {
        CostRecord costRecord = new CostRecord(
                CloudService.EC2,
                new BigDecimal("25.50"),
                LocalDate.of(2026,8,6)
        );

        assertThat(costRecord.getService()).isEqualTo(CloudService.EC2);
        assertThat(costRecord.getCost()).isEqualByComparingTo(new BigDecimal("25.50"));
        assertThat(costRecord.getUsageDate()).isEqualTo(LocalDate.of(2026,8,6));
        assertThat(costRecord.getSource()).isEqualTo(CostSource.MANUAL);
    }

    @Test
    void 비용이_음수면_예외발생() {
        assertThatThrownBy(() -> new CostRecord(
                CloudService.EC2,
                new BigDecimal("-1"),
                LocalDate.of(2026,8,6)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비용은 음수일 수 없습니다.");
    }

    @Test
    void 비용이_null이면_예외발생() {
        assertThatThrownBy(() -> new CostRecord(
                CloudService.EC2,
                null,
                LocalDate.of(2026,8,6)
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비용은 필수 입니다");
    }

    @Test
    void AWS_출저로_비용_기록_생성() {
        CostRecord costRecord = new CostRecord(
                CloudService.S3,
                new BigDecimal("10.5"),
                LocalDate.of(2026,8,1),
                CostSource.AWS_COST_EXPLORER
        );

        assertThat(costRecord.getSource()).isEqualTo(CostSource.AWS_COST_EXPLORER);
    }

    @Test
    void 비용_출저가_null이면_예외발생() {
        assertThatThrownBy(() -> new CostRecord(
                CloudService.S3,
                new BigDecimal("10.5"),
                LocalDate.of(2026,8,1),
                null
        )).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비용 출처는 필수입니다.");
    }
}
