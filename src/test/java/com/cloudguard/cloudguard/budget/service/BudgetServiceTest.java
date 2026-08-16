package com.cloudguard.cloudguard.budget.service;

import com.cloudguard.cloudguard.budget.domain.BudgetStatus;
import com.cloudguard.cloudguard.budget.domain.MonthlyBudget;
import com.cloudguard.cloudguard.budget.exception.DuplicateMonthlyBudgetException;
import com.cloudguard.cloudguard.budget.exception.MonthlyBudgetNotFoundException;
import com.cloudguard.cloudguard.budget.repository.MonthlyBudgetRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BudgetServiceTest {

    private final CostRecordRepository costRecordRepository;
    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final BudgetService budgetService;

    @Autowired
    BudgetServiceTest(CostRecordRepository costRecordRepository, MonthlyBudgetRepository monthlyBudgetRepository, BudgetService budgetService) {
        this.costRecordRepository = costRecordRepository;
        this.monthlyBudgetRepository = monthlyBudgetRepository;
        this.budgetService = budgetService;
    }

    @Test
    void 예산상태_반환() {

        CostRecord costRecord1 = new CostRecord(
                CloudService.EC2,
                BigDecimal.valueOf(200),
                LocalDate.of(2026,8,6)
        );
        CostRecord costRecord2 = new CostRecord(
                CloudService.S3,
                BigDecimal.valueOf(300),
                LocalDate.of(2026,8,7)
        );
        CostRecord costRecord3 = new CostRecord(
                CloudService.RDS,
                BigDecimal.valueOf(300),
                LocalDate.of(2026,8,8)
        );



        costRecordRepository.save(costRecord1);
        costRecordRepository.save(costRecord2);
        costRecordRepository.save(costRecord3);

        YearMonth yearMonth = YearMonth.of(2026,8);
        BigDecimal monthlyLimit = BigDecimal.valueOf(1000);

        monthlyBudgetRepository.save(new MonthlyBudget(yearMonth,monthlyLimit));

        BudgetStatus result = budgetService.determineMonthlyStatus(yearMonth);

        assertEquals(BudgetStatus.CAUTION,result);

    }

    @Test
    void 월_예산을_저장 () {

        YearMonth yearMonth = YearMonth.of(2026,8);
        BigDecimal monthlyLimit = BigDecimal.valueOf(1000);

        MonthlyBudget savedBudget = budgetService.addMonthlyBudget(yearMonth,monthlyLimit);

        MonthlyBudget foundBudget = monthlyBudgetRepository
                .findByYearMonth(yearMonth)
                .orElseThrow();

        assertThat(savedBudget.getId()).isNotNull();
        assertThat(foundBudget.getYearMonth())
                .isEqualTo(yearMonth);
        assertThat(foundBudget.getMonthlyLimit())
                .isEqualByComparingTo(monthlyLimit);

    }

    @Test
    void 등록되지_않은_연월의_예산상태를_조회하면_예외발생() {
        YearMonth yearMonth = YearMonth.of(2050,10);

        Assertions.assertThatThrownBy(() ->
                        budgetService.determineMonthlyStatus(yearMonth))
                .isInstanceOf(MonthlyBudgetNotFoundException.class)
                .hasMessage("해당 연월의 예산이 등록되어 있지 않습니다.");
    }

    @Test
    void 월예산_중복등록_예외발생() {
        YearMonth yearMonth = YearMonth.of(2026,8);
        BigDecimal monthlyLimit = BigDecimal.valueOf(1000);

        monthlyBudgetRepository.save( new MonthlyBudget(yearMonth,monthlyLimit));

        assertThatThrownBy(() ->
                budgetService.addMonthlyBudget(yearMonth,monthlyLimit))
                .isInstanceOf(DuplicateMonthlyBudgetException.class)
                .hasMessage("해당 연월의 예산이 이미 등록되어 있습니다.");

    }


}