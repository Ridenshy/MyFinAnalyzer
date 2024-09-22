package ru.Tim.Proj.moneyAnalyzer.Controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.Tim.Proj.moneyAnalyzer.Config.MyUserDetails;
import ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Other.HoldersService;
import ru.Tim.Proj.moneyAnalyzer.Models.HolderModels.BankAccount;
import ru.Tim.Proj.moneyAnalyzer.Models.HolderModels.CashAccount;
import ru.Tim.Proj.moneyAnalyzer.Models.HolderModels.DepositAccount;
import ru.Tim.Proj.moneyAnalyzer.Models.HolderModels.SavingsAccount;
import ru.Tim.Proj.moneyAnalyzer.Models.HolderModels.MoneyHolders;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;

import java.math.BigDecimal;
import java.time.LocalDate;

@Controller
@RequestMapping("/profile/holders")
public class HoldersInfoController {

    private final HoldersService holdersService;

    @Autowired
    public HoldersInfoController(HoldersService holdersService) {
        this.holdersService = holdersService;
    }

    @GetMapping
    public String getMoneyHoldersInfo(@AuthenticationPrincipal MyUserDetails userDetails,
                                      Model model){
        Long id = userDetails.getUser().getId();
        model.addAttribute("Holders", holdersService.getCashOrBank(id));
        model.addAttribute("Deposits", holdersService.getDepositOrSavings(id));
        model.addAttribute("totalBalance", holdersService.getBalance(id));
        model.addAttribute("totalBank", holdersService.getCashAndBankAmount(id));
        return "accountpages/moneyaccs";
    }


    @PostMapping("/cash")
    public String createCashHolder(@ModelAttribute("cashholder") @Valid CashAccount holder,
                               @AuthenticationPrincipal MyUserDetails myUserDetails){
        User user = myUserDetails.getUser();
        holder.setUser(user);
        holder.setActiveCheck(false);
        holdersService.createOrUpdateHolder(holder);
        return "redirect:/profile/holders";
    }

    @PostMapping("/bank")
    public String createBankHolder(@ModelAttribute("bankholder") @Valid BankAccount holder,
                                   @AuthenticationPrincipal MyUserDetails myUserDetails){
        User user = myUserDetails.getUser();
        holder.setUser(user);
        holder.setActiveCheck(false);
        holdersService.createOrUpdateHolder(holder);
        return "redirect:/profile/holders";
    }

    @PostMapping("/deposit")
    public String createDeposit(@ModelAttribute("deposit") @Valid DepositAccount deposit,
                                @AuthenticationPrincipal MyUserDetails myUserDetails){
        User user = myUserDetails.getUser();
        deposit.setUser(user);
        deposit.setActiveCheck(true);
        deposit.setNextInterestDate(deposit.calculateNextInterestDate(deposit.getOpenDate()));
        deposit.calcWhileNormalDate();
        holdersService.createOrUpdateHolder(deposit);
        return "redirect:/profile/holders";
    }

    @PostMapping("/savings")
    public String createSavings(@ModelAttribute("savings") @Valid SavingsAccount savings,
                                @RequestParam("openDateDay") Integer openDay,
                                @AuthenticationPrincipal MyUserDetails myUserDetails){
        User user = myUserDetails.getUser();
        savings.setUser(user);
        savings.setActiveCheck(true);
        if(LocalDate.now().isBefore(LocalDate.now().withDayOfMonth(openDay))){
            savings.setOpenDate(LocalDate.now().withDayOfMonth(openDay).minusMonths(1));
        }else {
            savings.setOpenDate(LocalDate.now().withDayOfMonth(openDay));
        }
        savings.setNextInterestDate(savings.getOpenDate());
        savings.calcWhileNormalDate();
        holdersService.createOrUpdateHolder(savings);
        return "redirect:/profile/holders";
    }

    @DeleteMapping
    public String deleteHolder(@RequestParam("id") Long id){
        MoneyHolders holder = holdersService.findHolderById(id);
        holdersService.deleteHolder(holder);
        return "redirect:/profile/holders";
    }

    @PatchMapping
    public String editHolder(
            @RequestParam("id") Long id,
            @RequestParam("type") String type,
            @RequestParam("amount") BigDecimal amount,
            @RequestParam(value = "creditLimit", required = false) BigDecimal creditLimit,
            @RequestParam("holderName") String holderName,
            @AuthenticationPrincipal MyUserDetails myUserDetails) {

        User user = myUserDetails.getUser();
        MoneyHolders holder;

        // Создаем конкретную реализацию на основе типа
        if ("BankAccount".equals(type)) {
            holder = new BankAccount();
            ((BankAccount) holder).setCreditLimit(creditLimit);
        } else if ("CashAccount".equals(type)) {
            holder = new CashAccount();
        } else {
            throw new IllegalArgumentException("Unknown type: " + type);
        }

        holder.setId(id);
        holder.setAmount(amount);
        holder.setHolderName(holderName);
        holder.setUser(user);
        holder.setTransactions(holdersService.findHolderById(id).getTransactions());

        holdersService.createOrUpdateHolder(holder);

        return "redirect:/profile/holders";
    }


    @PostMapping("/getMoney")
    public String getMoney(@RequestParam("holderGetterId")Long getterId,
                           @RequestParam("savingsAccountId") Long savingsId,
                           @RequestParam("action") String action,
                           @RequestParam("amount") BigDecimal amount){

        MoneyHolders getterAccount = holdersService.findHolderById(getterId);
        MoneyHolders savingsAccount = holdersService.findHolderById(savingsId);

        BigDecimal getterAmount = getterAccount.getAmount();
        BigDecimal savingsAmount = savingsAccount.getAmount();

        if(action.equals("subs")){
            getterAccount.setAmount(getterAmount.add(amount));
            savingsAccount.setAmount(savingsAmount.subtract(amount));
        }else {
            getterAccount.setAmount(getterAmount.subtract(amount));
            savingsAccount.setAmount(savingsAmount.add(amount));
        }

        holdersService.createOrUpdateHolder(savingsAccount);
        holdersService.createOrUpdateHolder(getterAccount);

        return "redirect:/profile/holders";
    }
}
