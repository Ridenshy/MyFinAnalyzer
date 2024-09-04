package ru.Tim.Proj.moneyAnalyzer.Models.HolderModels;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Entity
@DiscriminatorValue("FIXED_DEPOSIT")
public class DepositAccount extends MoneyHolders {

    @Column(name = "open_date")
    private LocalDate openDate;

    @Column(name = "term_months")
    private long termMonths;

    @Column(name = "interest_rate")
    private BigDecimal interestRate;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_period")
    private InterestPeriod interestPeriod;

    @Column(name = "capitalization")
    private boolean capitalization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id")
    private MoneyHolders holder;

    @Column(name = "additional_replenishments")
    private BigDecimal additionalReplenishments;

    @Column(name = "next_interest_date")
    private LocalDate nextInterestDate;

    @Column(name = "non_capitalized_interest")
    private BigDecimal nonCapitalizedInterest = BigDecimal.ZERO;

    public DepositAccount() {}

    public DepositAccount(Long id, String holderName, BigDecimal amount, LocalDate openDate, int termMonths,
                          BigDecimal interestRate, InterestPeriod interestPeriod, boolean capitalization,
                          MoneyHolders holder, BigDecimal additionalReplenishments, LocalDate nextInterestDate,
                          boolean activeCheck) {
        super(id, holderName, amount, activeCheck);
        this.openDate = openDate;
        this.termMonths = termMonths;
        this.interestRate = interestRate;
        this.interestPeriod = interestPeriod;
        this.capitalization = capitalization;
        this.holder = holder;
        this.additionalReplenishments = additionalReplenishments;
        this.nextInterestDate = nextInterestDate;
    }

    public LocalDate getOpenDate() { return openDate; }

    public void setOpenDate(LocalDate openDate) { this.openDate = openDate; }

    public long getTermMonths() { return termMonths; }

    public void setTermMonths(long termMonths) { this.termMonths = termMonths; }

    public BigDecimal getInterestRate() { return interestRate; }

    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }

    public InterestPeriod getInterestPeriod() { return interestPeriod; }

    public void setInterestPeriod(InterestPeriod interestPeriod) { this.interestPeriod = interestPeriod; }

    public boolean isCapitalization() { return capitalization; }

    public void setCapitalization(boolean capitalization) { this.capitalization = capitalization; }

    public MoneyHolders getHolder() { return holder; }

    public void setHolder(MoneyHolders holder) { this.holder = holder; }

    public BigDecimal getAdditionalReplenishments() { return additionalReplenishments; }

    public void setAdditionalReplenishments(BigDecimal additionalReplenishments) {
        this.additionalReplenishments = additionalReplenishments; }

    public LocalDate getNextInterestDate() { return nextInterestDate; }

    public void setNextInterestDate(LocalDate nextInterestDate) { this.nextInterestDate = nextInterestDate; }

    public BigDecimal getNonCapitalizedInterest() { return nonCapitalizedInterest; }

    public void setNonCapitalizedInterest(BigDecimal nonCapitalizedInterest) {
        this.nonCapitalizedInterest = nonCapitalizedInterest; }

    @Override
    public void calculateInterest() {
        LocalDate currentDate = LocalDate.now();

        if (!currentDate.isBefore(nextInterestDate)) {
            BigDecimal balance = getAmount();
            BigDecimal rate = switch (interestPeriod) {
                case MONTHLY ->interestRate.divide(BigDecimal.valueOf(1200), 8, RoundingMode.HALF_UP);
                case QUARTERLY -> interestRate.divide(BigDecimal.valueOf(400), 8, RoundingMode.HALF_UP);
                case YEARLY -> interestRate.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
                case END_OF_TERM -> interestRate.divide(BigDecimal.valueOf(100 * (termMonths / 12)), 8, RoundingMode.HALF_UP);
            };

            if (capitalization) {
                balance = balance.add(balance.multiply(rate));
            }
            else {
                nonCapitalizedInterest = nonCapitalizedInterest.add(balance.multiply(rate));
            }

            if (additionalReplenishments != null && additionalReplenishments.compareTo(BigDecimal.ZERO) > 0) {
                balance = balance.add(additionalReplenishments);
                additionalReplenishments = BigDecimal.ZERO;
            }

            setAmount(balance);

            nextInterestDate = calculateNextInterestDate(nextInterestDate);
        }
        if(!currentDate.isBefore(openDate.plusMonths(termMonths))){
            setActiveCheck(false);
            BigDecimal balance = getHolder().getAmount()
                                 .add(getAmount())
                                 .add((nonCapitalizedInterest));
            getHolder().setAmount(balance);
            setAmount(BigDecimal.ZERO);
            setNonCapitalizedInterest(BigDecimal.ZERO);
            setNextInterestDate(null);
        }
    }

    public LocalDate calculateNextInterestDate(LocalDate date) {
        return switch (interestPeriod) {
            case MONTHLY -> date.plusMonths(1);
            case QUARTERLY -> date.plusMonths(3);
            case YEARLY -> date.plusMonths(12);
            case END_OF_TERM -> openDate.plusMonths(termMonths);
        };
    }

    public void calcWhileNormalDate(){
        while(nextInterestDate.isBefore(LocalDate.now())){
            calculateInterest();
        }
    }

    public enum InterestPeriod {
        MONTHLY,
        QUARTERLY,
        YEARLY,
        END_OF_TERM
    }
}
