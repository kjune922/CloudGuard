package com.cloudguard.cloudguard.cost.repository;

import com.cloudguard.cloudguard.cost.domain.CloudService;
import com.cloudguard.cloudguard.cost.domain.CostRecord;
import com.cloudguard.cloudguard.cost.domain.CostSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CostRecordRepository extends JpaRepository<CostRecord,Long> {

    List<CostRecord> findByUsageDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );
    List<CostRecord> findByServiceAndUsageDateBetween(
            CloudService service,
            LocalDate startDate,
            LocalDate endDate
    );
    Optional<CostRecord> findByServiceAndUsageDateAndSource(
            CloudService service,
            LocalDate usageDate,
            CostSource source
    );
}
