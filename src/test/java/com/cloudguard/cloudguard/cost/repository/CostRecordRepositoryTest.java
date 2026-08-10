package com.cloudguard.cloudguard.cost.repository;

import com.cloudguard.cloudguard.cost.domain.CloudService;
import com.cloudguard.cloudguard.cost.domain.CostRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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

        costRecordRepository.save(costRecord);

        CostRecord findRecord = costRecordRepository.findById(costRecord.getId()).orElseThrow();

        assertThat(findRecord.getService()).isEqualTo(CloudService.EC2);
        assertThat(findRecord.getCost()).isEqualByComparingTo(BigDecimal.valueOf(100));
        assertThat(findRecord.getUsageDate()).isEqualTo(LocalDate.of(2026,8,10));
    }

    @Test
    void 특정_월의_비용을_가져오자() {

        CostRecord costRecord1 = new CostRecord(
                CloudService.EC2,
                BigDecimal.valueOf(200),
                LocalDate.of(2026,8,1)
        );

        CostRecord costRecord2 = new CostRecord(
                CloudService.S3,
                BigDecimal.valueOf(100),
                LocalDate.of(2026,8,31)
        );

        CostRecord costRecord3 = new CostRecord(
                CloudService.RDS,
                BigDecimal.valueOf(300),
                LocalDate.of(2026,7,5)
        );

        costRecordRepository.save(costRecord1);
        costRecordRepository.save(costRecord2);
        costRecordRepository.save(costRecord3);

        LocalDate startDate = LocalDate.of(2026,8,1);
        LocalDate endDate = LocalDate.of(2026,8,31);

        List<CostRecord> records = costRecordRepository.findByUsageDateBetween(startDate,endDate);

        assertThat(records.size()).isEqualTo(2);

        assertThat(records).extracting(CostRecord::getService).containsExactlyInAnyOrder(
                CloudService.EC2,
                CloudService.S3
        );
    }


}