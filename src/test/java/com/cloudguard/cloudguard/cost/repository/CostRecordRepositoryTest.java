package com.cloudguard.cloudguard.cost.repository;

import com.cloudguard.cloudguard.cost.domain.CloudService;
import com.cloudguard.cloudguard.cost.domain.CostRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
class CostRecordRepositoryTest {

    @Autowired
    CostRecordRepository costRecordRepository;

    @Test
    void 비용기록을_저장하고_조회함() {
        CostRecord costRecord = new CostRecord(
                CloudService.EC2,
                BigDecimal.valueOf(100),
                LocalDate.of(2026,8,10)
        );

        CostRecord saveRecord = costRecordRepository.save(costRecord);

        CostRecord findRecord = costRecordRepository.findById(costRecord.getId()).orElseThrow();

        assertThat(findRecord.getService()).isEqualTo(CloudService.EC2);
        assertThat(findRecord.getCost()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(findRecord.getUsageDate()).isEqualTo(LocalDate.of(2026,8,10));
    }

}