package ru.Tim.Proj.moneyAnalyzer.Models.HolderModels;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;

@Entity
@DiscriminatorValue("CASH")
public class CashAccount extends MoneyHolders {
    public CashAccount() {}

    public CashAccount(Long id, String holderName, BigDecimal amount, boolean activeCheck) {
        super(id, holderName, amount, activeCheck);
    }

    @Override
    public void calculateInterest() {}
}
