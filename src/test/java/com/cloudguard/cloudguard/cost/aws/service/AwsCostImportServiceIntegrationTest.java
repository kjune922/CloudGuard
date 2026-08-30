package com.cloudguard.cloudguard.cost.aws.service;


import com.cloudguard.cloudguard.cost.aws.dto.AwsServiceCost;
import com.cloudguard.cloudguard.cost.domain.CloudService;
import com.cloudguard.cloudguard.cost.domain.CostRecord;
import com.cloudguard.cloudguard.cost.domain.CostSource;
import com.cloudguard.cloudguard.cost.dto.AwsDailyServiceCost;
import com.cloudguard.cloudguard.cost.repository.CostRecordRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Transactional
public class AwsCostImportServiceIntegrationTest {

    @Autowired
    private AwsCostImportService awsCostImportService;

    @Autowired
    private CostRecordRepository costRecordRepository;

    @Autowired
    private EntityManager entityManager; // 변경 내용 DB에 반영

    @MockitoBean
    private AwsCostExplorerService awsCostExplorerService;

    @Test
    void AWS_비용을_DB에_실제로_저장() {
        LocalDate startDate = LocalDate.of(2026,8,1);
        LocalDate endDate = LocalDate.of(2026,8,31);

        given(awsCostExplorerService.getDailyServiceCosts(startDate,endDate))
                .willReturn(List.of(new AwsDailyServiceCost(
                        "Amazon Simple Storage Service",
                        new BigDecimal("10.5"),
                        "USD",
                        startDate
                )));

        awsCostImportService.importCosts(startDate,endDate);

        List<CostRecord> records = costRecordRepository.findByUsageDateBetween(startDate,endDate);
        assertThat(records).hasSize(1);
        assertThat(records.get(0).getService()).isEqualTo(CloudService.S3);
        assertThat(records.get(0).getCost()).isEqualByComparingTo("10.5");
        assertThat(records.get(0).getUsageDate()).isEqualTo(startDate);
        assertThat(records.get(0).getSource()).isEqualTo(CostSource.AWS_COST_EXPLORER);
    }

    @Test
    void 같은_기간을_두번_저장해도_비용은_중복되지않음 () {
        LocalDate startDate = LocalDate.of(2026,8,1);
        LocalDate endDate = LocalDate.of(2026,8,31);

        given(awsCostExplorerService.getDailyServiceCosts(startDate,endDate))
                .willReturn(List.of(new AwsDailyServiceCost(
                        "Amazon Simple Storage Service",
                        new BigDecimal("10.5"),
                        "USD",
                        startDate
                )));

        awsCostImportService.importCosts(startDate,endDate);
        awsCostImportService.importCosts(startDate,endDate);

        List<CostRecord> records = costRecordRepository.findByUsageDateBetween(startDate,endDate);

        assertThat(records).hasSize(1);
        assertThat(records.get(0).getService()).isEqualTo(CloudService.S3);
        assertThat(records.get(0).getCost()).isEqualByComparingTo("10.5");
        assertThat(records.get(0).getUsageDate()).isEqualTo(startDate);
        assertThat(records.get(0).getSource()).isEqualTo(CostSource.AWS_COST_EXPLORER);
    }

    @Test
    void AWS_비용을_재수집하면_기존기록의_금액갱신() {
        LocalDate startDate = LocalDate.of(2026,8,1);
        LocalDate endDate = LocalDate.of(2026,8,31);

        AwsDailyServiceCost firstCost = new AwsDailyServiceCost(
                "Amazon Simple Storage Service",
                new BigDecimal("10.5"),
                "USD",
                startDate
        );

        AwsDailyServiceCost updatedCost = new AwsDailyServiceCost(
                "Amazon Simple Storage Service",
                new BigDecimal("12"),
                "USD",
                startDate
        );

        given(awsCostExplorerService.getDailyServiceCosts(
                startDate,
                endDate
        )).willReturn(List.of(firstCost), List.of(updatedCost));

        // 첫번째 수집 - 10.5 저장함
        awsCostImportService.importCosts(startDate,endDate);

        Long firstId = costRecordRepository.findByServiceAndUsageDateAndSource(
                CloudService.S3,
                startDate,
                CostSource.AWS_COST_EXPLORER
        ).orElseThrow().getId();

        // 2번째 수집 - 12로 갱신함
        awsCostImportService.importCosts(startDate,endDate);

        // 변경내용 DB에 반영 이후 관리중인 엔티티 싹비움
        entityManager.flush(); // 변경 내용 DB에 반영 (커밋은 아님)
        entityManager.clear(); // 관리중인 엔티티 비움 (DB 데이터 삭제는 아님)

        List<CostRecord> records = costRecordRepository.findByUsageDateBetween(startDate,endDate);

        assertThat(records).hasSize(1);

        CostRecord result = records.get(0);

        // 여기서 Id를 확인하는 이유는 기존 기록을 삭제하고 새로 만드는게 아닌 같은 기록의 금액만 변경했는지 체크하기위해서임
        assertThat(result.getId()).isEqualTo(firstId);
        assertThat(result.getService()).isEqualTo(CloudService.S3);
        assertThat(result.getCost()).isEqualByComparingTo("12");
        assertThat(result.getUsageDate()).isEqualTo(startDate);
        assertThat(result.getSource()).isEqualTo(CostSource.AWS_COST_EXPLORER);
    }

    @Test
    void AWS_비용을_갱신해도_수동등록_비용유지() {
        LocalDate startDate = LocalDate.of(2026,8,1);
        LocalDate endDate = LocalDate.of(2026,8,31);

        CostRecord manualRecord = costRecordRepository.save(
                new CostRecord(
                        CloudService.S3,
                        new BigDecimal("100"),
                        startDate
                )
        );

        CostRecord awsRecord = costRecordRepository.save(
                new CostRecord(
                        CloudService.S3,
                        new BigDecimal("10.5"),
                        startDate,
                        CostSource.AWS_COST_EXPLORER
                )
        );

        Long manualId = manualRecord.getId();
        Long awsId = awsRecord.getId();

        given(awsCostExplorerService.getDailyServiceCosts(startDate,endDate));

        awsCostImportService.importCosts(startDate, endDate);

        entityManager.flush();
        entityManager.clear();

        List<CostRecord> records = costRecordRepository.findByUsageDateBetween(
                startDate,endDate
        );

        assertThat(records).hasSize(2);

        CostRecord manualResult = costRecordRepository.findById(manualId).orElseThrow();

        CostRecord awsResult = costRecordRepository.findById(awsId).orElseThrow();

        assertThat(manualResult.getSource()).isEqualTo(CostSource.MANUAL);
        assertThat(manualResult.getCost()).isEqualByComparingTo("100");

        assertThat(awsResult.getSource()).isEqualTo(CostSource.AWS_COST_EXPLORER);
        assertThat(awsResult.getCost()).isEqualByComparingTo("12");

    }
}
