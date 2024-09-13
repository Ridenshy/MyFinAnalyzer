package ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Other;


import org.springframework.stereotype.Service;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.Transaction;
import ru.Tim.Proj.moneyAnalyzer.Repositoryes.Other.TransactionRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TransactionService {


    private final TransactionRepository transactionRepository;

    public TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaction findTransaction(Long id) {
        return transactionRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Transaction not found with id: " + id));
    }

    public void deleteTransaction(Transaction transaction) {
        transactionRepository.delete(transaction);
    }

    public void createOrUpdateTransaction(Transaction transaction) {
        transactionRepository.save(transaction);
    }

    public List<Transaction> getUserTransactionList(Long userId) {
        return transactionRepository.findByUserId(userId);
    }

    public List<Transaction> getFilteredTransactions(LocalDate startDate, LocalDate endDate,
                                                     BigDecimal minAmount, BigDecimal maxAmount,
                                                     Long holderId, Long userId,
                                                     List<Long> expenseCategoryIds,
                                                     List<Long> incomeSourceIds,
                                                     String typeOfTransfer) {
        Transaction.TransferType transferTypeEnum = null;
        if (typeOfTransfer != null && !typeOfTransfer.isEmpty()) {
            if (typeOfTransfer.equalsIgnoreCase("INCOME") || typeOfTransfer.equalsIgnoreCase("EXPENSE")) {
                transferTypeEnum = Transaction.TransferType.valueOf(typeOfTransfer.toUpperCase());
            }
        }

        return transactionRepository.getFiltratedList(startDate, endDate, minAmount, maxAmount,
                holderId, userId, expenseCategoryIds, incomeSourceIds, transferTypeEnum);
    }

    public Map<Long, BigDecimal> getTotalAmountsExp(Long userId, String yearMonth) {
        List<Object[]> results = transactionRepository.findTotalAmountsByCategory(userId, yearMonth);
        return results.stream()
                .collect(Collectors.toMap(result -> (Long) result[0], result -> (BigDecimal) result[1]));
    }

    public Map<Long, BigDecimal> getTotalAmountsInc(Long userId, String yearMonth){
        List<Object[]> results = transactionRepository.findTotalAmountsBySource(userId, yearMonth);
        return results.stream()
                .collect(Collectors.toMap(result -> (Long) result[0], result -> (BigDecimal) result[1]));
    }

    public Map<String, BigDecimal> getCategoryNameAndAmount(Long userId, String yearMonth){
        List<Object[]> results = transactionRepository.findTotalAmountsByCategoryNames(userId, yearMonth);
        return results.stream()
                .collect(Collectors.toMap(result -> (String) result[0], result -> (BigDecimal) result[1]));
    }

    public Map<String, BigDecimal> getSourceNameAndAmount(Long userId, String yearMonth){
        List<Object[]> results = transactionRepository.findTotalAmountsBySourceNames(userId, yearMonth);
        return results.stream()
                .collect(Collectors.toMap(result -> (String) result[0], result -> (BigDecimal) result[1]));
    }

    public Map<Integer, BigDecimal> getYearMonthAmounts(Long id, String date){
        List<Object[]> results = transactionRepository.getYearMonthAmounts(id, date);
        return results.stream()
                .collect(Collectors.toMap(result -> (Integer) result[0], result -> (BigDecimal) result[1]));
    }

    public Map<Integer, BigDecimal> getMonthAmount(Long id, String date){
        List<Object[]> results = transactionRepository.getDaysOfMonthSum(id, date);

        Map<Integer, BigDecimal> resultMap = results.stream()
                .collect(Collectors.toMap(
                        result -> (Integer) result[0],
                        result -> (BigDecimal) result[1],
                        (existing, replacement) -> existing
                ));
        Integer lastDay = resultMap.keySet().stream()
                .max(Integer::compareTo)
                .orElse(0);
        if (lastDay == 0) {
            return resultMap;
        }
        Map<Integer, BigDecimal> filledMap = new HashMap<>();
        for (int day = 1; day <= lastDay; day++) {
            filledMap.put(day, resultMap.getOrDefault(day, BigDecimal.ZERO));
        }
        return filledMap;
    }

    public Map<Integer, BigDecimal> getMonthAmountGrowth(Long id, String date, Transaction.TransferType transferType) {
        List<Object[]> results = transactionRepository.getMonthTransactionsExp(id, date, transferType);

        Map<Integer, BigDecimal> cumulativeMa = new LinkedHashMap<>();
        BigDecimal cumulativeSum = BigDecimal.ZERO;

        YearMonth yearMonth = YearMonth.parse(date);
        int daysInMonth = yearMonth.lengthOfMonth();

        Integer lastDay = 0;

        if(!results.isEmpty()){
            lastDay = (Integer) results.get(results.size() - 1)[0];
        }

        for (int day = 1; day <= daysInMonth; day++) {
            if(day <= lastDay){
                cumulativeMa.put(day, cumulativeSum);
            }
            else {
                if (transferType.name().equals("INCOME")) {
                    cumulativeMa.put(day, cumulativeSum);
                } else {
                    cumulativeMa.put(day, BigDecimal.ONE.negate());
                }
            }
        }

        for (Object[] result : results) {
            Integer day = (Integer) result[0];
            BigDecimal amount = (BigDecimal) result[1];
            cumulativeSum = cumulativeSum.add(amount);
            cumulativeMa.put(day, cumulativeSum);
        }

        BigDecimal lastValue = BigDecimal.ZERO;
        for (Map.Entry<Integer, BigDecimal> entry : cumulativeMa.entrySet()) {
            if (entry.getValue().equals(BigDecimal.ZERO)) {
                cumulativeMa.put(entry.getKey(), lastValue);
            } else {
                lastValue = entry.getValue();
            }
        }

        return cumulativeMa;
    }

}
