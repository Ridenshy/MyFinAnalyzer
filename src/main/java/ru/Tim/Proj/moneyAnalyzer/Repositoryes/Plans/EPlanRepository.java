package ru.Tim.Proj.moneyAnalyzer.Repositoryes.Plans;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;
import ru.Tim.Proj.moneyAnalyzer.Models.Plan.PlannedExpense;

import java.util.List;

public interface EPlanRepository extends JpaRepository<PlannedExpense, Long> {

    @Query("SELECT e FROM PlannedExpense e WHERE e.user.id = :userId AND e.yearMonth = :planDate ORDER BY e.expenseCategory.id ASC")
    List<PlannedExpense> getCurrentMonthExpPlans(@Param("userId") Long id,
                                               @Param("planDate") String yearMonth);

    List<PlannedExpense> findAllByUser(User user);

    @Transactional
    @Modifying
    @Query("DELETE FROM PlannedExpense p WHERE p.user = :user")
    void deleteAllByUser(@Param("user") User user);

}
