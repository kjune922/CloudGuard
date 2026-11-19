package com.cloudguard.cloudguard.budget.domain;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class BudgetPolicyTest {

    @ParameterizedTest
    @CsvSource({
            "100, 69.999, SAFE", "100, 70, CAUTION", "100, 70.001, CAUTION",
            "100, 84.999, CAUTION", "100, 85, WARNING", "100, 85.001, WARNING",
            "100, 99.999, WARNING", "100, 100, EXCEEDED", "100, 100.001, EXCEEDED",
            "3, 2.099999999999999999, SAFE", "3, 2.1, CAUTION",
            "3, 2.549999999999999999, CAUTION", "3, 2.55, WARNING",
            "0.01, 0.0069999, SAFE", "0.01, 0.007, CAUTION",
            "10000000000000000000, 6999999999999999999.999, SAFE"
    })
    void 반올림하지_않은_실제_금액으로_경계를_판정한다(
            String limit, String cost, BudgetStatus expected) {
        BudgetPolicy policy = new BudgetPolicy(new BigDecimal(limit));
        assertThat(policy.determineStatus(new BigDecimal(cost))).isEqualTo(expected);
    }

    @Test
    void 표시용_사용률이_반올림되어도_실제_상태는_경계_미만이다() {
        BudgetPolicy policy = new BudgetPolicy(new BigDecimal("100"));
        BigDecimal cost = new BigDecimal("69.999");
        assertThat(policy.calculateUsageRate(cost)).isEqualByComparingTo("70");
        assertThat(policy.determineStatus(cost)).isEqualTo(BudgetStatus.SAFE);
    }

    @Test
    void 사용률이_70퍼센트_미만이면_SAFE다() {
        BudgetPolicy policy = new BudgetPolicy(BigDecimal.valueOf(100));

        BudgetStatus status =
                policy.determineStatus(BigDecimal.valueOf(69));

        assertThat(status).isEqualTo(BudgetStatus.SAFE);
    }

    @Test
    void 사용률이_70퍼센트_이상이면_CAUTION이다() {
        BudgetPolicy policy = new BudgetPolicy(BigDecimal.valueOf(100));

        BudgetStatus status =
                policy.determineStatus(BigDecimal.valueOf(70));

        assertThat(status).isEqualTo(BudgetStatus.CAUTION);
    }

    @Test
    void 사용률이_85퍼센트_이상이면_WARNING이다() {
        BudgetPolicy policy = new BudgetPolicy(BigDecimal.valueOf(100));

        BudgetStatus status =
                policy.determineStatus(BigDecimal.valueOf(85));

        assertThat(status).isEqualTo(BudgetStatus.WARNING);
    }

    @Test
    void 사용률이_100퍼센트_이상이면_EXCEEDED다() {
        BudgetPolicy policy = new BudgetPolicy(BigDecimal.valueOf(100));

        BudgetStatus status =
                policy.determineStatus(BigDecimal.valueOf(100));

        assertThat(status).isEqualTo(BudgetStatus.EXCEEDED);
    }

    @Test
    void 월_예산이_0이면_생성할_수_없다() {
        assertThatThrownBy(
                () -> new BudgetPolicy(BigDecimal.ZERO)
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("월 예산은 0보다 커야 합니다 !!");
    }

    @Test
    void 현재_비용이_음수이면_계산할_수_없다() {
        BudgetPolicy policy = new BudgetPolicy(BigDecimal.valueOf(100));

        assertThatThrownBy(
                () -> policy.determineStatus(BigDecimal.valueOf(-1))
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("현재 비용은 0 이상이어야 합니다 !!");
    }

    @Test
    void 월_예산과_현재비용으로_사용률을_계산한다() {
        BudgetPolicy policy = new BudgetPolicy(BigDecimal.valueOf(10000));

        BigDecimal usageRate = policy.calculateUsageRate(BigDecimal.valueOf(5000));

        assertThat(usageRate).isEqualByComparingTo(BigDecimal.valueOf(50));
    }
}
