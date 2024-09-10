package ru.Tim.Proj.moneyAnalyzer.Repositoryes.Category;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.Tim.Proj.moneyAnalyzer.Models.Category.IncomeSource;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;

import java.util.List;


public interface IncomeRepository extends JpaRepository<IncomeSource, Long> {

    @Query("SELECT i FROM IncomeSource i WHERE i.user.id IS NULL OR i.user.id = :userId")
    List<IncomeSource> getSources(@Param("userId") Long id);

    @Query("SELECT i FROM IncomeSource i WHERE i.user.id = :userId")
    List<IncomeSource> getOnlyUserSources(@Param("userId") Long id);

    @Query("SELECT COUNT(*) FROM IncomeSource i")
    Integer getSourceAmount();

    @Query("SELECT i FROM IncomeSource i WHERE i.sourceName = :sourceName AND (i.user = :user OR i.user IS NULL)")
    IncomeSource findBySourceNameAndUser(@Param("sourceName") String sourceName, @Param("user") User user);

    @Transactional
    @Modifying
    @Query("DELETE FROM IncomeSource i WHERE i.user = :user")
    void deleteAllByUser(@Param("user") User user);

}
