package ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Other;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.Tim.Proj.moneyAnalyzer.Models.HolderModels.BankAccount;
import ru.Tim.Proj.moneyAnalyzer.Models.HolderModels.CashAccount;
import ru.Tim.Proj.moneyAnalyzer.Models.HolderModels.MoneyHolders;
import ru.Tim.Proj.moneyAnalyzer.Models.HolderModels.SavingsAccount;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;
import ru.Tim.Proj.moneyAnalyzer.Repositoryes.Other.HolderRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;

@Service
public class HoldersService {

    private final HolderRepository holderRepository;

    public void starterHolders(User user){
        BankAccount bankAccount = new BankAccount();
        CashAccount cashAccount = new CashAccount();
        bankAccount.setHolderName("Основной счет");
        cashAccount.setHolderName("Наличные");
        bankAccount.setAmount(BigDecimal.ZERO);
        cashAccount.setAmount(BigDecimal.ZERO);
        bankAccount.setUser(user);
        cashAccount.setUser(user);
        bankAccount.setCreditLimit(BigDecimal.ZERO);

        holderRepository.save(bankAccount);
        holderRepository.save(cashAccount);
    }
    public HoldersService(HolderRepository holderRepository) {
        this.holderRepository = holderRepository;
    }

    public void createOrUpdateHolder(MoneyHolders holder) {
        holderRepository.save(holder);
    }

    public void deleteHolder(MoneyHolders holder){
        holderRepository.delete(holder);
    }
    public MoneyHolders findHolderById(Long id){
        return holderRepository.findById(id)
            .orElseThrow(() -> new NoSuchElementException("Transaction not found with id: " + id));
    }

    @Transactional
    public void calculateInterestForAllAccounts(){
        List<MoneyHolders> accounts = holderRepository.getDepositsAndSavings();
        for(MoneyHolders account : accounts){
            if(account.getActiveCheck()){
                account.calculateInterest();
                holderRepository.save(account);
            }
        }

    }

    @PostConstruct
    public void calculateWhileNormalPost(){
        List<MoneyHolders> accounts = holderRepository.getDepositsAndSavings();
        for(MoneyHolders holder : accounts){
            if(holder instanceof SavingsAccount savingsAccount && savingsAccount.getActiveCheck()){
                savingsAccount.calcWhileNormalDate();
                holderRepository.save(holder);
                System.out.println("проценты посчитаны до текущей даты");
            }
        }
    }

    @Transactional
    public void updateDayMinAmount(){
        List<MoneyHolders> accounts = holderRepository.getDepositsAndSavings();
        for(MoneyHolders account : accounts){
            if(account instanceof SavingsAccount savingsAccount
                    && savingsAccount.getUpdateDate().isBefore(LocalDate.now())
                    && savingsAccount.getActiveCheck()){
                savingsAccount.setMinAmount(account.getAmount());
                holderRepository.save(account);
            }
        }
    }

    public List<MoneyHolders> getHolderList(User user){ return holderRepository.findAllByUser(user); }

    public List<MoneyHolders> getCashOrBank(Long userId){ return holderRepository.findAllCashOrBank(userId); }

    public List<MoneyHolders> getDepositOrSavings(Long userId) { return holderRepository.findAllDepositOrSavings(userId); }

    public BigDecimal getBalance(Long userId) { return holderRepository.getTotalBalance(userId); }

    public BigDecimal getCashAndBankAmount(Long userId){return holderRepository.getCashAndBankAmount(userId); }

}
