package com.cloudguard.cloudguard.cost.service;

import com.cloudguard.cloudguard.cost.domain.CloudService;
import com.cloudguard.cloudguard.cost.domain.CostRecord;
import com.cloudguard.cloudguard.cost.domain.MonthlyCost;
import com.cloudguard.cloudguard.cost.repository.CostRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class CostService {

    private final CostRecordRepository costRecordRepository;

    @Autowired
    public CostService(CostRecordRepository costRecordRepository) {
        this.costRecordRepository = costRecordRepository;
    }

    public BigDecimal calculateMonthlyCost(YearMonth yearMonth){

        if(yearMonth == null){
            throw new IllegalArgumentException("조회할 연월은 필수입니다.");
        }

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<CostRecord> records = costRecordRepository.findByUsageDateBetween(
                startDate,
                endDate
        );
        MonthlyCost monthlyCost = new MonthlyCost(records);
        return monthlyCost.calculateTotal(yearMonth);
    }

    public BigDecimal calculateMonthlyCostByService(YearMonth yearMonth,CloudService service){

        if(yearMonth == null){
            throw new IllegalArgumentException("조회할 연월은 필수입니다.");
        }

        if (service == null) {
            throw new IllegalArgumentException("조회할 서비스는 필수입니다.");
        }

        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<CostRecord> records = costRecordRepository.findByServiceAndUsageDateBetween(
                service,
                startDate,
                endDate
        );
        MonthlyCost monthlyCost = new MonthlyCost(records);
        return monthlyCost.calculateTotal(yearMonth);
    }

    public CostRecord addServiceCost(CloudService cloudService, BigDecimal cost, LocalDate usageDate){
        CostRecord costRecord = new CostRecord(cloudService,cost,usageDate);
        return costRecordRepository.save(costRecord);
    }

}
