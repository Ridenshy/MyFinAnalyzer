package ru.Tim.Proj.moneyAnalyzer.Repositoryes.Other;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.Tim.Proj.moneyAnalyzer.Models.HolderModels.MoneyHolders;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;

import java.math.BigDecimal;
import java.util.List;

public interface HolderRepository extends JpaRepository<MoneyHolders, Long> {

    @Query("SELECT m FROM MoneyHolders m WHERE TYPE(m) IN (CashAccount, BankAccount) AND m.user.id = :userId ORDER BY m.id ASC")
    List<MoneyHolders> findAllCashOrBank(@Param("userId") Long userId);

    @Query("SELECT m FROM MoneyHolders m WHERE TYPE(m) IN (DepositAccount, SavingsAccount) AND m.user.id = :userId ORDER BY m.id ASC")
    List<MoneyHolders> findAllDepositOrSavings(@Param("userId") Long userId);

    @Query("SELECT m FROM MoneyHolders m WHERE TYPE(m) IN (DepositAccount, SavingsAccount)")
    List<MoneyHolders> getDepositsAndSavings();

    @Query("SELECT SUM(m.amount) FROM MoneyHolders m WHERE m.user.id = :userId")
    BigDecimal getTotalBalance(@Param("userId") Long userId);

    List<MoneyHolders> findAllByUser(User user);

    MoneyHolders findFirstByHolderNameAndUser(String holderName, User user);

    @Transactional
    @Modifying
    @Query("DELETE FROM MoneyHolders h WHERE h.user = :user")
    void deleteAllByUser(@Param("user") User user);

}
