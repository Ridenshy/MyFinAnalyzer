package ru.Tim.Proj.moneyAnalyzer.Repositoryes.Category;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.Tim.Proj.moneyAnalyzer.Models.Category.ExpenseCategory;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;

import java.util.List;

public interface ExpenseRepository extends JpaRepository<ExpenseCategory, Long> {


    @Query("SELECT e FROM ExpenseCategory e WHERE e.user.id IS NULL OR e.user.id = :userId")
    List<ExpenseCategory> getCategories(@Param("userId") Long id);

    @Query("SELECT e FROM ExpenseCategory e WHERE e.user.id = :userId")
    List<ExpenseCategory> getOnlyUserCategories(@Param("userId") Long id);

    @Query("SELECT COUNT(*) FROM ExpenseCategory e")
    Integer getCategoryAmount();

    @Query("SELECT e FROM ExpenseCategory e WHERE e.categoryName = :categoryName AND (e.user = :user OR e.user IS NULL)")
    ExpenseCategory findByCategoryNameAndUser(@Param("categoryName") String categoryName, @Param("user") User user);

    @Transactional
    @Modifying
    @Query("DELETE FROM ExpenseCategory e WHERE e.user = :user")
    void deleteAllByUser(@Param("user") User user);
}
