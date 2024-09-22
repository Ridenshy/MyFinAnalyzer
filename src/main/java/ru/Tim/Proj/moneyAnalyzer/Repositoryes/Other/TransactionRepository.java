package ru.Tim.Proj.moneyAnalyzer.Repositoryes.Other;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.Transaction;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserId(Long userId);

    @Query("SELECT t FROM Transaction t WHERE " +
            "(COALESCE(:startDate, t.transactionDate) IS NULL OR t.transactionDate >= COALESCE(:startDate, t.transactionDate)) AND " +
            "(COALESCE(:endDate, t.transactionDate) IS NULL OR t.transactionDate <= COALESCE(:endDate, t.transactionDate)) AND " +
            "(:minAmount IS NULL OR t.amount >= :minAmount) AND " +
            "(:maxAmount IS NULL OR t.amount <= :maxAmount) AND " +
            "(:holderId IS NULL OR t.moneyHolders.id = :holderId) AND " +
            "(:userId IS NULL OR t.user.id = :userId) AND " +
            "(:expenseCategories IS NULL OR t.expenseCategory.id IN :expenseCategories) AND " +
            "(:incomeSources IS NULL OR t.incomeSource.id IN :incomeSources) AND " +
            "(:typeOfTransfer IS NULL OR t.typeOfTransfer = :typeOfTransfer) ORDER BY t.transactionDate DESC, t.id DESC")
    List<Transaction> getFiltratedList(@Param("startDate") LocalDate startDate,
                                       @Param("endDate") LocalDate endDate,
                                       @Param("minAmount") BigDecimal minAmount,
                                       @Param("maxAmount") BigDecimal maxAmount,
                                       @Param("holderId") Long holderId, // Добавлено
                                       @Param("userId") Long userId,
                                       @Param("expenseCategories") List<Long> expenseCategories,
                                       @Param("incomeSources") List<Long> incomeSources,
                                       @Param("typeOfTransfer") Transaction.TransferType typeOfTransfer);

    @Query("SELECT t.expenseCategory.id, SUM(t.amount) " +
            "FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND DATE_TRUNC('month', t.transactionDate) = TO_DATE(:yearMonth, 'YYYY-MM') " +
            "AND t.typeOfTransfer = 'EXPENSE' " +
            "GROUP BY t.expenseCategory.id")
    List<Object[]> findTotalAmountsByCategory(@Param("userId") Long userId,
                                              @Param("yearMonth") String yearMonth);

    @Query("SELECT t.incomeSource.id, SUM(t.amount) " +
            "FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND DATE_TRUNC('month', t.transactionDate) = TO_DATE(:yearMonth, 'YYYY-MM') " +
            "AND t.typeOfTransfer = 'INCOME' " +
            "GROUP BY t.incomeSource.id")
    List<Object[]> findTotalAmountsBySource(@Param("userId") Long userId,
                                            @Param("yearMonth") String yearMonth);

    @Query("SELECT e.categoryName, SUM(t.amount) " +
            "FROM Transaction t " +
            "JOIN t.expenseCategory e " +
            "WHERE t.user.id = :userId " +
            "AND DATE_TRUNC('month', t.transactionDate) = TO_DATE(:yearMonth, 'YYYY-MM') " +
            "AND t.typeOfTransfer = 'EXPENSE' " +
            "GROUP BY e.categoryName")
    List<Object[]> findTotalAmountsByCategoryNames(@Param("userId") Long userId,
                                                   @Param("yearMonth") String yearMonth);

    @Query("SELECT i.sourceName, SUM(t.amount) " +
            "FROM Transaction t " +
            "JOIN t.incomeSource i " +
            "WHERE t.user.id = :userId " +
            "AND DATE_TRUNC('month', t.transactionDate) = TO_DATE(:yearMonth, 'YYYY-MM') " +
            "AND t.typeOfTransfer = 'INCOME' " +
            "GROUP BY i.sourceName")
    List<Object[]> findTotalAmountsBySourceNames(@Param("userId") Long userId,
                                                 @Param("yearMonth") String yearMonth);

    @Query("SELECT EXTRACT(DAY FROM t.transactionDate) AS day, t.amount " +
            "FROM Transaction t WHERE t.user.id = :userId " +
            "AND DATE_TRUNC('month', t.transactionDate) = TO_DATE(:date, 'YYYY-MM') " +
            "AND t.typeOfTransfer = :transType GROUP BY EXTRACT(DAY FROM t.transactionDate), t.amount")
    List<Object[]> getMonthTransactionsExp(@Param("userId") Long id,
                                           @Param("date") String date,
                                           @Param("transType") Transaction.TransferType transferType);


    @Query("SELECT EXTRACT(DAY FROM t.transactionDate) AS day, SUM(t.amount) " +
            "FROM Transaction t WHERE t.user.id = :userId " +
            "AND DATE_TRUNC('month', t.transactionDate) = TO_DATE(:date, 'YYYY-MM') " +
            "AND t.typeOfTransfer = 'EXPENSE' GROUP BY EXTRACT(DAY FROM t.transactionDate)")
    List<Object[]> getDaysOfMonthSum(@Param("userId") Long id,
                                   @Param("date") String date);


    @Query("SELECT EXTRACT(MONTH FROM t.transactionDate) AS month, SUM(t.amount) " +
            "FROM Transaction t " +
            "WHERE t.user.id = :userId " +
            "AND DATE_TRUNC('year', t.transactionDate) = TO_DATE(:date, 'YYYY') " +
            "AND t.typeOfTransfer = 'EXPENSE' " +
            "GROUP BY EXTRACT(MONTH FROM t.transactionDate)")
    List<Object[]> getYearMonthAmounts(@Param("userId") Long id,
                                       @Param("date") String date);

    @Transactional
    @Modifying
    @Query("DELETE FROM Transaction t WHERE t.user = :user")
    void deleteAllByUser(@Param("user") User user);
}

