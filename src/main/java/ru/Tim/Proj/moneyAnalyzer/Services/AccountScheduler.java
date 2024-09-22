package ru.Tim.Proj.moneyAnalyzer.Services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Other.HoldersService;
import ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Other.VerifiService;

@Service
@EnableScheduling
public class AccountScheduler {

    private final HoldersService holdersService;
    private final VerifiService verifiService;

    @PostConstruct
    public void startCalc(){
        holdersService.calculateInterestForAllAccounts();
        System.out.println("Проценты успешно посчитаны");
    }

    @Autowired
    public AccountScheduler(HoldersService holdersService, VerifiService verifiService) {
        this.holdersService = holdersService;
        this.verifiService = verifiService;
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void calculateInterestForAllAccounts() {
        holdersService.calculateInterestForAllAccounts();
    }

    @Scheduled(cron = "0 0 0/1 * * *")
    public void deleteOldTokens(){
        verifiService.deleteTokens();
        System.out.println("Старые токены удалены");
    }
}
