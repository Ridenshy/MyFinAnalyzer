package ru.Tim.Proj.moneyAnalyzer.Models.Category;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.Transaction;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;

import java.util.Set;

@Entity
@Table(name = "expense_categories")
public class ExpenseCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(min = 0, max = 30, message = "слишком длинное название")
    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;

    @OneToMany(mappedBy = "expenseCategory", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<Transaction> transactions;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acc_id")
    private User user;

    public ExpenseCategory() {}

    public ExpenseCategory(String categoryName, User user) {
        this.categoryName = categoryName;
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Set<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(Set<Transaction> transactions) {
        this.transactions = transactions;
    }
}
