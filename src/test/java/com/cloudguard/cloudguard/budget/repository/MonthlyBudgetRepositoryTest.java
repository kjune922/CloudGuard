package com.cloudguard.cloudguard.budget.repository;

import com.cloudguard.cloudguard.budget.domain.MonthlyBudget;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.YearMonth;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class MonthlyBudgetRepositoryTest {

    @Autowired
    private MonthlyBudgetRepository monthlyBudgetRepository;

    @Test
    void 연월로_월예산_조회 () {
        MonthlyBudget monthlyBudget = new MonthlyBudget(
                YearMonth.of(2026,8),
                BigDecimal.valueOf(1000)
        );

        monthlyBudgetRepository.save(monthlyBudget);
        MonthlyBudget result = monthlyBudgetRepository
                .findByYearMonth(YearMonth.of(2026,8))
                .orElseThrow();

        assertThat(result.getId()).isNotNull();
        assertThat(result.getYearMonth())
                .isEqualTo(YearMonth.of(2026,8));
        assertThat(result.getMonthlyLimit())
                .isEqualByComparingTo(BigDecimal.valueOf(1000));
    }
}