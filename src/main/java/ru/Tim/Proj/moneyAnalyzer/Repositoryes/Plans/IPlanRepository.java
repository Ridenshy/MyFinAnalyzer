package ru.Tim.Proj.moneyAnalyzer.Repositoryes.Plans;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.Tim.Proj.moneyAnalyzer.Models.Plan.PlannedIncome;

import java.util.List;

public interface iPlanRepository extends JpaRepository<PlannedIncome, Long> {

    @Query("SELECT i FROM PlannedIncome i WHERE i.user.id = :userId AND i.yearMonth = :planDate ORDER BY i.incomeSource.id ASC")
    List<PlannedIncome> getCurrentMonthIncPlans(@Param("userId") Long id,
                                                @Param("planDate") String yearMonth);

}
