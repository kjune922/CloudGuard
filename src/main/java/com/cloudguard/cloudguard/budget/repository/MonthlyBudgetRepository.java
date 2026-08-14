package com.cloudguard.cloudguard.budget.repository;

import com.cloudguard.cloudguard.budget.domain.MonthlyBudget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.YearMonth;
import java.util.Optional;

/**
 *  save(monthlyBudget)
 *  findById(id);
 *  findAll();
 *  delete(monthlyBudget);
 *
 *  위 4가지 기능 가능케함
 */

public interface MonthlyBudgetRepository extends JpaRepository<MonthlyBudget, Long> {
                                                              // MonthlyBudge = 저장할 엔티티, Long = 엔티티의 ID타입
    Optional<MonthlyBudget> findByYearMonth(YearMonth yearMonth);
    // Spring Data Jpa가 메서드 이름 분석 후
    // SELECT * FROM monthly_budgets WHERE yearMonth = ? 라는 쿼리를 만듬
    // Optional의 사용이유 : 요청한 달의 예산이 아직 등록 안되었을 때 대비

    // Spring Data JPA가 메서드 이름을 분석해 해당 연월의 데이터가 존재하는지 조회해줌
    boolean existsByYearMonth(YearMonth yearMonth);
}
