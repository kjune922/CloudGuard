package com.cloudguard.cloudguard.budget.service;

import com.cloudguard.cloudguard.budget.domain.BudgetStatus;
import com.cloudguard.cloudguard.cost.domain.CloudService;
import com.cloudguard.cloudguard.cost.domain.CostRecord;
import com.cloudguard.cloudguard.cost.repository.CostRecordRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class BudgetServiceTest {

    private final CostRecordRepository costRecordRepository;
    private final BudgetService budgetService;

    @Autowired
    BudgetServiceTest(CostRecordRepository costRecordRepository, BudgetService budgetService) {
        this.costRecordRepository = costRecordRepository;
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

        BudgetStatus result = budgetService.determineMonthlyStatus(yearMonth,monthlyLimit);

        assertEquals(BudgetStatus.CAUTION,result);



    }

}