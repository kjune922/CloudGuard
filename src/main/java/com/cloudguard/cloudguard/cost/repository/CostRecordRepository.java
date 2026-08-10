package com.cloudguard.cloudguard.cost.repository;

import com.cloudguard.cloudguard.cost.domain.CostRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CostRecordRepository extends JpaRepository<CostRecord,Long> {
}
