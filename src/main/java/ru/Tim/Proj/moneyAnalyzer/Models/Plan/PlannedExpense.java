package ru.Tim.Proj.moneyAnalyzer.Models.Plan;

import jakarta.persistence.*;
import ru.Tim.Proj.moneyAnalyzer.Models.Category.ExpenseCategory;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;

import java.math.BigDecimal;

@Entity
@Table(name = "planned_expenses")
public class PlannedExpense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id")
    private ExpenseCategory expenseCategory;

    @Column(nullable = false)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acc_id", nullable = false)
    private User user;

    @Column(name = "plan_month", nullable = false)
    private String yearMonth;

    public PlannedExpense() {};

    public PlannedExpense(Long id, ExpenseCategory expenseCategory, BigDecimal amount,
                          User user, String yearMonth) {
        this.id = id;
        this.expenseCategory = expenseCategory;
        this.amount = amount;
        this.user = user;
        this.yearMonth = yearMonth;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getYearMonth() {
        return yearMonth;
    }

    public void setYearMonth(String yearMonth) {
        this.yearMonth = yearMonth;
    }

    public ExpenseCategory getExpenseCategory() {
        return expenseCategory;
    }

    public void setExpenseCategory(ExpenseCategory expenseCategory) {
        this.expenseCategory = expenseCategory;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
