package ru.Tim.Proj.moneyAnalyzer.Models.HolderModels;


import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("BANK")
public class BankAccount extends MoneyHolders{

    @Column(name = "bank_limit")
    private BigDecimal creditLimit;

    public BankAccount() {}

    public BankAccount(Long id, String holderName, BigDecimal amount, BigDecimal creditLimit, boolean activeCheck) {
        super(id, holderName, amount, activeCheck);
        this.creditLimit = creditLimit;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    @Override
    public void calculateInterest() {}
}
