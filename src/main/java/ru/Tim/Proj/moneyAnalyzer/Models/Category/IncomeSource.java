package ru.Tim.Proj.moneyAnalyzer.Models.Category;


import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.Transaction;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;

import java.util.Set;

@Entity
@Table(name = "income_sources")
public class IncomeSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 0, max = 30, message = "слишком длинное название")
    @Column(name = "source_name", nullable = false, length = 100)
    private String sourceName;

    @OneToMany(mappedBy = "incomeSource", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Transaction> transactions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acc_id")
    private User user;

    public IncomeSource() {}

    public IncomeSource(String sourceName, User user) {
        this.sourceName = sourceName;
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    public Set<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(Set<Transaction> transactions) {
        this.transactions = transactions;
    }
}
