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
}
