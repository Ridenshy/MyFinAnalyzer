package ru.Tim.Proj.moneyAnalyzer.Models.HolderModels;


import jakarta.persistence.*;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.Transaction;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "money_holder")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "holder_type")
public abstract class MoneyHolders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Size(min = 0, max = 30, message = "Имя слишком длинное")
    @NotEmpty(message = "Введите имя счета")
    @Column(name = "holder_name")
    private String holderName;

    @Column(name = "holder_amount")
    @Digits(integer = 38, fraction = 2, message = "введено не верное число")
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "acc_id", nullable = false)
    private User user;

    @Column(name = "active_check")
    private boolean activeCheck;

    @OneToMany(mappedBy = "moneyHolders", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions;

    public MoneyHolders() {}

    public MoneyHolders(Long id, String holderName, BigDecimal amount, boolean activeCheck) {
        this.id = id;
        this.holderName = holderName;
        this.amount = amount;
        this.activeCheck = activeCheck;
    }

    public boolean getActiveCheck() {
        return activeCheck;
    }

    public void setActiveCheck(boolean activeCheck) {
        this.activeCheck = activeCheck;
    }

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public String getHolderName() { return holderName; }

    public void setHolderName(String holderName) { this.holderName = holderName; }

    public BigDecimal getAmount() { return amount; }

    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }

    public List<Transaction> getTransactions() { return transactions; }

    public void setTransactions(List<Transaction> transactions) { this.transactions = transactions; }

    public abstract void calculateInterest();
}
