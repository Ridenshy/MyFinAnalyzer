package ru.Tim.Proj.moneyAnalyzer.Repositoryes.Plans;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.Tim.Proj.moneyAnalyzer.Models.Plan.PlannedExpense;

import java.util.List;

public interface ePlanRepository extends JpaRepository<PlannedExpense, Long> {

    @Query("SELECT e FROM PlannedExpense e WHERE e.user.id = :userId AND e.yearMonth = :planDate ORDER BY e.expenseCategory.id ASC")
    List<PlannedExpense> getCurrentMonthExpPlans(@Param("userId") Long id,
                                               @Param("planDate") String yearMonth);
}
