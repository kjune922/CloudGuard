package com.cloudguard.cloudguard.cost.repository;

import com.cloudguard.cloudguard.cost.domain.CloudService;
import com.cloudguard.cloudguard.cost.domain.CostRecord;
import com.cloudguard.cloudguard.cost.domain.CostSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
    @Test
    void 특정_월의_특정_서비스_비용만_조회() {
        CostRecord marchEc2 = new CostRecord(
                CloudService.EC2,
                BigDecimal.valueOf(3000),
                LocalDate.of(2027, 3, 1)
        );

        CostRecord marchRds = new CostRecord(
                CloudService.RDS,
                BigDecimal.valueOf(2000),
                LocalDate.of(2027, 3, 15)
        );

        CostRecord aprilEc2 = new CostRecord(
                CloudService.EC2,
                BigDecimal.valueOf(9000),
                LocalDate.of(2027, 4, 1)
        );

        costRecordRepository.save(marchEc2);
        costRecordRepository.save(marchRds);
        costRecordRepository.save(aprilEc2);

        List<CostRecord> records =
                costRecordRepository.findByServiceAndUsageDateBetween(
                        CloudService.EC2,
                        LocalDate.of(2027, 3, 1),
                        LocalDate.of(2027, 3, 31)
                );

        assertThat(records).hasSize(1);
        assertThat(records.get(0).getService())
                .isEqualTo(CloudService.EC2);
        assertThat(records.get(0).getCost())
                .isEqualByComparingTo(BigDecimal.valueOf(3000));
        assertThat(records.get(0).getUsageDate())
                .isEqualTo(LocalDate.of(2027, 3, 1));
    }

    @Test
    void 같은_서비스와_날짜에서도_AWS_출저의_기록만_조회 () {
        LocalDate usageDate = LocalDate.of(2026,8,1);

        CostRecord manualRecord = new CostRecord(CloudService.S3, new BigDecimal("100"), usageDate);

        CostRecord awsRecord = new CostRecord(
                CloudService.S3,
                new BigDecimal("10.5"),
                usageDate,
                CostSource.AWS_COST_EXPLORER
        );

        costRecordRepository.save(manualRecord);
        costRecordRepository.save(awsRecord);

        CostRecord result = costRecordRepository.findByServiceAndUsageDateAndSource(
                CloudService.S3,
                usageDate,
                CostSource.AWS_COST_EXPLORER
        ).orElseThrow();

        assertThat(result.getId()).isEqualTo(awsRecord.getId());
        assertThat(result.getSource()).isEqualTo(awsRecord.getSource());
        assertThat(result.getCost()).isEqualByComparingTo("10.5");
    }

    @Test
    void 수동_기록만_있으면_AWS_기록_조회결과는_비어있음 () {
        LocalDate usageDate = LocalDate.of(2026,8,1);

        costRecordRepository.save(new CostRecord(
                CloudService.S3,
                new BigDecimal("100"),
                usageDate
        ));

        Optional<CostRecord> result = costRecordRepository.findByServiceAndUsageDateAndSource(
                CloudService.S3,
                usageDate,
                CostSource.AWS_COST_EXPLORER
        );
        assertThat(result).isEmpty();
    }
}