package ru.Tim.Proj.moneyAnalyzer.Repositoryes.Category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.Tim.Proj.moneyAnalyzer.Models.Category.ExpenseCategory;
import ru.Tim.Proj.moneyAnalyzer.Models.Category.IncomeSource;

import java.util.List;


public interface IncomeRepository extends JpaRepository<IncomeSource, Long> {

    @Query("SELECT i FROM IncomeSource i WHERE i.user.id IS NULL OR i.user.id = :userId")
    List<IncomeSource> getSources(@Param("userId") Long id);

    @Query("SELECT i FROM IncomeSource i WHERE i.user.id = :userId")
    List<IncomeSource> getOnlyUserSources(@Param("userId") Long id);

    @Query("SELECT COUNT(*) FROM IncomeSource i")
    Integer getSourceAmount();
}
