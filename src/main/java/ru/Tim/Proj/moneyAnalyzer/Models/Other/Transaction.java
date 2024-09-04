package ru.Tim.Proj.moneyAnalyzer.Models.Other;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import ru.Tim.Proj.moneyAnalyzer.Models.Category.ExpenseCategory;
import ru.Tim.Proj.moneyAnalyzer.Models.Category.IncomeSource;
import ru.Tim.Proj.moneyAnalyzer.Models.HolderModels.MoneyHolders;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Digits(integer = 38, fraction = 2, message = "введено не верное число")
    @Positive(message = "сумма должна быть положительной")
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @Column(name = "type_of_transfer", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private TransferType typeOfTransfer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acc_id", nullable = false)
    private User user;

    @Column(name = "trans_comment")
    private String transComment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "income_source_id")
    private IncomeSource incomeSource;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_category_id")
    private ExpenseCategory expenseCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "holder_id")
    private MoneyHolders moneyHolders;

    public Transaction() {}

    public Transaction(LocalDate transactionDate, BigDecimal amount, TransferType typeOfTransfer,
                       User user, String transComment, IncomeSource incomeSource,
                       ExpenseCategory expenseCategory, MoneyHolders moneyHolders) {
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.typeOfTransfer = typeOfTransfer;
        this.user = user;
        this.transComment = transComment;
        this.incomeSource = incomeSource;
        this.expenseCategory = expenseCategory;
        this.moneyHolders = moneyHolders;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public LocalDate getTransactionDate() { return transactionDate; }

    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }

    public BigDecimal getAmount() { return amount; }

    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TransferType getTypeOfTransfer() { return typeOfTransfer; }

    public void setTypeOfTransfer(TransferType typeOfTransfer) { this.typeOfTransfer = typeOfTransfer; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public String getTransComment() { return transComment; }

    public void setTransComment(String transComment) { this.transComment = transComment; }

    public IncomeSource getIncomeSource() { return incomeSource; }

    public void setIncomeSource(IncomeSource incomeSource) { this.incomeSource = incomeSource; }

    public ExpenseCategory getExpenseCategory() { return expenseCategory; }

    public void setExpenseCategory(ExpenseCategory expenseCategory) { this.expenseCategory = expenseCategory; }

    public MoneyHolders getMoneyHolders() { return moneyHolders; }

    public void setMoneyHolders(MoneyHolders moneyHolders) { this.moneyHolders = moneyHolders; }

    public enum TransferType { INCOME, EXPENSE }

}
