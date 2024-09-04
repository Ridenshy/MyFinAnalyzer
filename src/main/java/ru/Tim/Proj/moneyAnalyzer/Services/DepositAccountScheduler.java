package ru.Tim.Proj.moneyAnalyzer.Services;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Other.HoldersService;

@Service
@EnableScheduling
public class DepositAccountScheduler {

    private final HoldersService holdersService;

    @PostConstruct
    public void startCalc(){
        System.out.println("Deposits is calculated");
        holdersService.calculateInterestForAllAccounts();}

    @Autowired
    public DepositAccountScheduler(HoldersService holdersService) {
        this.holdersService = holdersService;
    }

    @Scheduled(cron = "0 0 0 * * ?") // Запуск каждый день в полночь
    public void calculateInterestForAllAccounts() {
        holdersService.calculateInterestForAllAccounts();
    }
}
