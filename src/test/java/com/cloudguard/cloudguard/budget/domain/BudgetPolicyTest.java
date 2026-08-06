package com.cloudguard.cloudguard.budget.domain;


import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class BudgetPolicyTest {

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
}
