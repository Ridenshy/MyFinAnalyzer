package ru.Tim.Proj.moneyAnalyzer.Models.Plan;

import jakarta.persistence.*;
import ru.Tim.Proj.moneyAnalyzer.Models.Category.IncomeSource;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;

import java.math.BigDecimal;

@Entity
@Table(name = "planned_incomes")
public class PlannedIncome {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "income_id")
    private IncomeSource incomeSource;

    @Column(nullable = false)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acc_id", nullable = false)
    private User user;

    @Column(name = "plan_date", nullable = false)
    private String yearMonth;

    public PlannedIncome() {};

    public PlannedIncome(Long id, IncomeSource incomeSource, BigDecimal amount,
                         User user, String yearMonth) {
        this.id = id;
        this.incomeSource = incomeSource;
        this.amount = amount;
        this.user = user;
        this.yearMonth = yearMonth;
    }

    public Long getId() {
        return id;
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

    public void setId(Long id) {
        this.id = id;
    }

    public IncomeSource getIncomeSource() {
        return incomeSource;
    }

    public void setIncomeSource(IncomeSource incomeSource) {
        this.incomeSource = incomeSource;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
