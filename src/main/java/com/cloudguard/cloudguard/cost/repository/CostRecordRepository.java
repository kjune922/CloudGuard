package com.cloudguard.cloudguard.cost.repository;

import com.cloudguard.cloudguard.cost.domain.CostRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CostRecordRepository extends JpaRepository<CostRecord,Long> {

    List<CostRecord> findByUsageDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );

}
