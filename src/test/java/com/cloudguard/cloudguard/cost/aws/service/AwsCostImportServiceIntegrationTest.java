package com.cloudguard.cloudguard.cost.aws.service;

import com.cloudguard.cloudguard.budget.domain.BudgetStatus;
import com.cloudguard.cloudguard.budget.dto.BudgetStatusResponse;
import com.cloudguard.cloudguard.budget.service.BudgetService;
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
import java.time.YearMonth;
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

    @Autowired
    private BudgetService budgetService;

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

        given(awsCostExplorerService.getDailyServiceCosts(startDate,endDate))
                .willReturn(List.of(new AwsDailyServiceCost(
                        "Amazon Simple Storage Service",
                        new BigDecimal("12"),
                        "USD",
                        startDate
                )));

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

    @Test
    void 겹치는_기간을_재수집하면_해당_날짜만_갱신() {
        LocalDate day10 = LocalDate.of(2026,8,10);
        LocalDate day11 = LocalDate.of(2026,8,11);
        LocalDate day12 = LocalDate.of(2026,8,12);
        LocalDate day13 = LocalDate.of(2026,8,13);

        // 1번째 조회 - 8.10 , 8.11 비용
        given(awsCostExplorerService.getDailyServiceCosts(day10,day12))
                .willReturn(List.of(
                        new AwsDailyServiceCost(
                        "Amazon Simple Storage Service",
                        new BigDecimal("3"),
                        "USD",
                        day10),
                        new AwsDailyServiceCost(
                                "Amazon Simple Storage Service",
                                new BigDecimal("5"),
                                        "USD",
                                        day11)
                        ));

        // 2번째 조회 - 8.11, 8.12 비용
        given(awsCostExplorerService.getDailyServiceCosts(
                day11,
                day13
        )).willReturn(List.of(
                new AwsDailyServiceCost(
                        "Amazon Simple Storage Service",
                        new BigDecimal("7"),
                        "USD",
                        day11
                ),
                new AwsDailyServiceCost(
                        "Amazon Simple Storage Service",
                        new BigDecimal("2"),
                        "USD",
                        day12
                )
        ));

        // 첫번째 수집
        awsCostImportService.importCosts(day10, day12);

        Long originalDay11Id = costRecordRepository
                .findByServiceAndUsageDateAndSource(
                        CloudService.S3,
                        day11,
                        CostSource.AWS_COST_EXPLORER
                )
                .orElseThrow()
                .getId();

        entityManager.flush();
        entityManager.clear();

        // 겹치는 기간 재수집 진행
        awsCostImportService.importCosts(day11,day13);

        entityManager.flush();
        entityManager.clear();

        // Repository의 Between은 양끝 날짜 다 포함
        List<CostRecord> records =
                costRecordRepository.findByUsageDateBetween(day10, day12);

        assertThat(records).hasSize(3);

        CostRecord day10Record = costRecordRepository
                .findByServiceAndUsageDateAndSource(
                        CloudService.S3,
                        day10,
                        CostSource.AWS_COST_EXPLORER
                ).orElseThrow();

        CostRecord day11Record = costRecordRepository
                .findByServiceAndUsageDateAndSource(
                        CloudService.S3,
                        day11,
                        CostSource.AWS_COST_EXPLORER
                ).orElseThrow();

        CostRecord day12Record = costRecordRepository
                .findByServiceAndUsageDateAndSource(
                        CloudService.S3,
                        day12,
                        CostSource.AWS_COST_EXPLORER
                ).orElseThrow();

        // 2번째 조회 범위 밖의 기록은 유지
        assertThat(day10Record.getCost())
                .isEqualByComparingTo("3");

        // 겹치는 날짜는 기존 ID 유지하면서 금액 갱신
        assertThat(day11Record.getId())
                .isEqualTo(originalDay11Id);
        assertThat(day11Record.getCost())
                .isEqualByComparingTo("7");

        // 새로운 날짜는 추가함
        assertThat(day12Record.getCost())
                .isEqualByComparingTo("2");
    }

    @Test
    void AWS의_작은_소수_비용도_DB보존() {
        LocalDate startDate = LocalDate.of(2026, 8, 10);
        LocalDate endDate = LocalDate.of(2026, 8, 11);
        BigDecimal amount = new BigDecimal("0.0000000488");

        given(awsCostExplorerService.getDailyServiceCosts(
                startDate, endDate
        )).willReturn(List.of(
                new AwsDailyServiceCost(
                        "Amazon Simple Storage Service",
                        amount,
                        "USD",
                        startDate
                )
        ));

        awsCostImportService.importCosts(startDate, endDate);

        entityManager.flush();
        entityManager.clear();

        CostRecord savedRecord =
                costRecordRepository.findByServiceAndUsageDateAndSource(
                        CloudService.S3,
                        startDate,
                        CostSource.AWS_COST_EXPLORER
                ).orElseThrow();

        assertThat(savedRecord.getCost())
                .isEqualByComparingTo(amount);
    }

    @Test
    void AWS_비용을_재수집하면_예산_사용률과_상태도_갱신() {
        YearMonth yearMonth = YearMonth.of(2026, 8);
        LocalDate startDate = LocalDate.of(2026, 8, 10);
        LocalDate endDate = LocalDate.of(2026, 8, 11);

        budgetService.addMonthlyBudget(
                yearMonth,
                new BigDecimal("100")
        );

        AwsDailyServiceCost firstCost = new AwsDailyServiceCost(
                "Amazon Simple Storage Service",
                new BigDecimal("80"),
                "USD",
                startDate
        );

        AwsDailyServiceCost updatedCost = new AwsDailyServiceCost(
                "Amazon Simple Storage Service",
                new BigDecimal("110"),
                "USD",
                startDate
        );

        given(awsCostExplorerService.getDailyServiceCosts(
                startDate, endDate
        )).willReturn(
                List.of(firstCost),
                List.of(updatedCost)
        );

        // 최초 수집: 비용 80 / 예산 100
        awsCostImportService.importCosts(startDate, endDate);

        entityManager.flush();
        entityManager.clear();

        BudgetStatusResponse firstStatus =
                budgetService.determineMonthlyStatusDetail(yearMonth);

        assertThat(firstStatus.getMonthlyLimit())
                .isEqualByComparingTo("100");
        assertThat(firstStatus.getTotalCost())
                .isEqualByComparingTo("80");
        assertThat(firstStatus.getUsageRate())
                .isEqualByComparingTo("80");
        assertThat(firstStatus.getStatus())
                .isEqualTo(BudgetStatus.CAUTION);

        // 같은 기간 재수집: 기존 비용을 110으로 갱신
        awsCostImportService.importCosts(startDate, endDate);

        entityManager.flush();
        entityManager.clear();

        BudgetStatusResponse updatedStatus =
                budgetService.determineMonthlyStatusDetail(yearMonth);

        assertThat(updatedStatus.getMonthlyLimit())
                .isEqualByComparingTo("100");
        assertThat(updatedStatus.getTotalCost())
                .isEqualByComparingTo("110");
        assertThat(updatedStatus.getUsageRate())
                .isEqualByComparingTo("110");
        assertThat(updatedStatus.getStatus())
                .isEqualTo(BudgetStatus.EXCEEDED);
    }
}
