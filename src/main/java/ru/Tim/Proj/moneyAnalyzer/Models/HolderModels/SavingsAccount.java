package ru.Tim.Proj.moneyAnalyzer.Models.HolderModels;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;


@Entity
@DiscriminatorValue("SAVINGS")
public class SavingsAccount extends MoneyHolders {

    @Column(name = "open_date")
    private LocalDate openDate;

    @Column(name = "interest_rate")
    private BigDecimal interestRate;

    @Column(name = "next_interest_date")
    private LocalDate nextInterestDate;

    @Column(name = "non_capitalized_interest")
    private BigDecimal nonCapitalizedInterest = BigDecimal.ZERO;

    @Column(name = "drop_date")
    private LocalDate dropDate;

    public SavingsAccount() {}

    public SavingsAccount(Long id, String holderName, BigDecimal amount, boolean activeCheck,
                          LocalDate openDate, BigDecimal interestRate,
                          LocalDate nextInterestDate, LocalDate dropDate) {
        super(id, holderName, amount, activeCheck);
        this.openDate = openDate;
        this.interestRate = interestRate;
        this.nextInterestDate = nextInterestDate;
        this.dropDate = dropDate;
    }

    public LocalDate getDropDate() { return dropDate; }

    public void setDropDate(LocalDate dropDate) { this.dropDate = dropDate; }

    public LocalDate getOpenDate() { return openDate; }

    public void setOpenDate(LocalDate openDate) { this.openDate = openDate; }

    public BigDecimal getInterestRate() { return interestRate; }

    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public LocalDate getNextInterestDate() { return nextInterestDate; }

    public void setNextInterestDate(LocalDate nextInterestDate) { this.nextInterestDate = nextInterestDate; }

    public BigDecimal getNonCapitalizedInterest() { return nonCapitalizedInterest; }

    public void setNonCapitalizedInterest(BigDecimal nonCapitalizedInterest) { this.nonCapitalizedInterest = nonCapitalizedInterest; }

    @Override
    public void calculateInterest() {
        LocalDate currentDate = LocalDate.now();

        if (!currentDate.isBefore(nextInterestDate) && !currentDate.isEqual(nextInterestDate)) {

            BigDecimal balance = getAmount();
            BigDecimal rate = interestRate.divide(BigDecimal.valueOf(36500), 8, RoundingMode.HALF_UP);
            nonCapitalizedInterest = nonCapitalizedInterest.add(balance.multiply(rate));
            nextInterestDate = nextInterestDate.plusDays(1);
            dropDate = currentDate.with(currentDate.plusMonths(1));
        }

        if(!currentDate.isBefore(dropDate)){
            BigDecimal balance = getAmount();
            setAmount(balance.add(nonCapitalizedInterest));
            nonCapitalizedInterest = BigDecimal.ZERO;
        }
    }

    public void calcWhileNormalDate(){
        while(nextInterestDate.isBefore(LocalDate.now())){
            calculateInterest();
        }
    }

}
