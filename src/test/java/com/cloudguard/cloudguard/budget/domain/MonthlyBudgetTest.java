package com.cloudguard.cloudguard.budget.domain;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.YearMonth;
import static org.assertj.core.api.Assertions.*;
import com.cloudguard.cloudguard.budget.exception.WrongRequestBadRequestException;

class MonthlyBudgetTest {

    @Test
    void 월_예산을_생성 () {
        MonthlyBudget monthlyBudget = new MonthlyBudget(
                YearMonth.of(2026,8),
        BigDecimal.valueOf(1000)
        );

        assertThat(monthlyBudget.getYearMonth())
                .isEqualTo(YearMonth.of(2026,8));
        assertThat(monthlyBudget.getMonthlyLimit())
                .isEqualByComparingTo(BigDecimal.valueOf(1000));
    }

    @Test
    void 연월은_null일수없음() {
        assertThatThrownBy(() ->
                new MonthlyBudget(null,BigDecimal.valueOf(1000)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("연월은 필수입니다.");
    }

    @Test
    void 월_예산은_null일수없음 () {
        assertThatThrownBy(() ->
                new MonthlyBudget(YearMonth.of(2026,8),null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("월 예산은 0보다 커야합니다.");
    }

    @Test
    void 월_예산은_0보다_커야함 () {
        assertThatThrownBy(() ->
                new MonthlyBudget(YearMonth.of(2026,8),BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("월 예산은 0보다 커야합니다.");
    }

    @Test
    void 월_예산은_음수안됨 () {
        assertThatThrownBy(() ->
                new MonthlyBudget(YearMonth.of(2026,8),BigDecimal.valueOf(-100)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("월 예산은 0보다 커야합니다.");
    }

    @Test
    void 월_예산한도_변경 () {
        MonthlyBudget monthlyBudget = new MonthlyBudget(
                YearMonth.of(2026,10),
                BigDecimal.valueOf(2000)
        );

        monthlyBudget.updateMonthlyLimit(BigDecimal.valueOf(3000));

        assertThat(monthlyBudget.getMonthlyLimit()).isEqualByComparingTo(BigDecimal.valueOf(3000));
    }

    @Test
    void 월예산_0으로_변경불가 () {
        MonthlyBudget monthlyBudget = new MonthlyBudget(
                YearMonth.of(2026,10),
                BigDecimal.valueOf(2000)
        );

        assertThatThrownBy(() ->
                monthlyBudget.updateMonthlyLimit(BigDecimal.valueOf(0)))
                .isInstanceOf(WrongRequestBadRequestException.class)
                .hasMessage("월 예산은 0보다 커야 합니다.");
    }

}