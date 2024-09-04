package ru.Tim.Proj.moneyAnalyzer.Repositoryes.Category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.Tim.Proj.moneyAnalyzer.Models.Category.ExpenseCategory;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<ExpenseCategory, Long> {


    @Query("SELECT e FROM ExpenseCategory e WHERE e.user.id IS NULL OR e.user.id = :userId")
    List<ExpenseCategory> getCategories(@Param("userId") Long id);

    @Query("SELECT e FROM ExpenseCategory e WHERE e.user.id = :userId")
    List<ExpenseCategory> getOnlyUserCategories(@Param("userId") Long id);

    @Query("SELECT COUNT(*) FROM ExpenseCategory e")
    Integer getCategoryAmount();

}
