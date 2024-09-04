package ru.Tim.Proj.moneyAnalyzer.Controllers;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.Tim.Proj.moneyAnalyzer.Config.MyUserDetails;
import ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Category.ExpenseCategoryService;
import ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Other.HoldersService;
import ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Category.IncomeSourceService;
import ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Other.TransactionService;
import ru.Tim.Proj.moneyAnalyzer.Models.Category.ExpenseCategory;
import ru.Tim.Proj.moneyAnalyzer.Models.Category.IncomeSource;
import ru.Tim.Proj.moneyAnalyzer.Models.HolderModels.MoneyHolders;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.Transaction;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/profile/operations")
public class TransactionsController {

    ExpenseCategoryService expenseCategoryService;
    IncomeSourceService incomeSourceService;
    TransactionService transactionService;
    HoldersService holdersService;

    @Autowired
    public TransactionsController(ExpenseCategoryService expenseCategoryService,
                                  IncomeSourceService incomeSourceService,
                                  TransactionService transactionService,
                                  HoldersService holdersService) {
        this.expenseCategoryService = expenseCategoryService;
        this.incomeSourceService = incomeSourceService;
        this.transactionService = transactionService;
        this.holdersService = holdersService;
    }

    @GetMapping
    public String OperationPage(@ModelAttribute("transaction") Transaction transaction,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                @RequestParam(required = false) BigDecimal minAmount,
                                @RequestParam(required = false) BigDecimal maxAmount,
                                @RequestParam(name ="holderId", required = false) Long holderId,
                                @RequestParam(name ="expC", required = false) List<Long> expenseCategories,
                                @RequestParam(name = "incC", required = false) List<Long> incomeSources,
                                @RequestParam(name = "incOrExp", required = false) String typeOfTransfer,
                                @AuthenticationPrincipal MyUserDetails userDetails,
                                Model model){
        Long id = userDetails.getUser().getId();
        model.addAttribute("expenseCategories", expenseCategoryService.getCategoryList(id));
        model.addAttribute("incomeSources", incomeSourceService.getCategoryList(id));
        model.addAttribute("customCategoryList", expenseCategoryService.getUserCategoryList(id));
        model.addAttribute("customSourceList", incomeSourceService.getUserSourcesList(id));
        model.addAttribute("moneyHolders", holdersService.getCashOrBank(userDetails.getUser().getId()));
        model.addAttribute("transactionList", transactionService.getFilteredTransactions(
                startDate, endDate, minAmount, maxAmount, holderId, id,
                expenseCategories, incomeSources, typeOfTransfer));

        // Фильтр
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("minAmount", minAmount);
        model.addAttribute("maxAmount", maxAmount);
        model.addAttribute("holderId", holderId);
        model.addAttribute("selectedExpenseCategories", expenseCategories);
        model.addAttribute("selectedIncomeSources", incomeSources);
        model.addAttribute("typeOfTransfer", typeOfTransfer);

        return "accountpages/operations";
    }


    @PostMapping
    public String AddTransaction(@ModelAttribute("transaction") @Valid Transaction transaction,
                                 @AuthenticationPrincipal MyUserDetails userDetails, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return "accountpages/operations";
        }
        BigDecimal updatedBalance;
        MoneyHolders holder = holdersService.findHolderById(transaction.getMoneyHolders().getId());

        if(transaction.getTypeOfTransfer().toString().equals("EXPENSE")){
            updatedBalance = holder.getAmount().subtract(transaction.getAmount());
            holder.setAmount(updatedBalance);
        }else{
            updatedBalance = holder.getAmount().add(transaction.getAmount());
            holder.setAmount(updatedBalance);
        }

        User user = userDetails.getUser();
        transaction.setUser(user);

        holdersService.createOrUpdateHolder(holder);
        transactionService.createOrUpdateTransaction(transaction);
        return "redirect:/profile/operations";
    }

    @PostMapping("/add-expCategory")
    public String addCategory(@ModelAttribute("expCategory") @Valid ExpenseCategory category,
                              BindingResult bindingResult,
                              @AuthenticationPrincipal MyUserDetails myUserDetails){
        if(bindingResult.hasErrors()){
            return "accountpages/operations";
        }
        category.setUser(myUserDetails.getUser());
        expenseCategoryService.createOrUpdate(category);

        return "redirect:/profile/operations";
    }

    @PostMapping("/add-incomeSource")
    public String addSource(@ModelAttribute("incSource") @Valid IncomeSource source,
                              BindingResult bindingResult,
                              @AuthenticationPrincipal MyUserDetails myUserDetails){
        if(bindingResult.hasErrors()){
            return "accountpages/operations";
        }
        source.setUser(myUserDetails.getUser());
        incomeSourceService.createOrUpdate(source);

        return "redirect:/profile/operations";
    }

    @PatchMapping("/editExpCategory")
    public String editCategory(@RequestParam("editId") Long id,
                               @RequestParam("editName") String name,
                               RedirectAttributes redirectAttributes){
        if(name.length() >= 30){
            redirectAttributes.addFlashAttribute("nameError", "Слишком длинное название");
            return "redirect:/profile/operations";
        }
        ExpenseCategory expenseCategory = expenseCategoryService.findCategoryById(id);
        expenseCategory.setCategoryName(name);
        expenseCategoryService.createOrUpdate(expenseCategory);
        return "redirect:/profile/operations";
    }

    @PatchMapping("/editIncSource")
    public String editSource(@RequestParam("editId") Long id,
                             @RequestParam("editName") String name,
                             RedirectAttributes redirectAttributes){
        if(name.length() >= 30){
            redirectAttributes.addFlashAttribute("nameError", "Слишком длинное название");
            return "redirect:/profile/operations";
        }
        IncomeSource incomeSource = incomeSourceService.findCategoryById(id);
        incomeSource.setSourceName(name);
        incomeSourceService.createOrUpdate(incomeSource);
        return "redirect:/profile/operations";
    }

    @DeleteMapping("/deleteExpCategory")
    public String deleteCategory(@RequestParam("deleteId") Long id){
        expenseCategoryService.deleteCategory(id);
        return "redirect:/profile/operations";
    }

    @DeleteMapping("/deleteIncSource")
    public String deleteSource(@RequestParam("deleteId") Long id){
        incomeSourceService.deleteCategory(id);
        return "redirect:/profile/operations";
    }
    
    @DeleteMapping
    public String DeleteTransaction(@RequestParam("id") Long id){

        Transaction transaction = transactionService.findTransaction(id);
        BigDecimal updatedBalance;
        MoneyHolders holder = holdersService.findHolderById(transaction.getMoneyHolders().getId());

        if(transaction.getTypeOfTransfer().toString().equals("INCOME")){
            updatedBalance = holder.getAmount().subtract(transaction.getAmount());
            holder.setAmount(updatedBalance);
        }else{
            updatedBalance = holder.getAmount().add(transaction.getAmount());
            holder.setAmount(updatedBalance);
        }


        holdersService.createOrUpdateHolder(holder);
        transactionService.deleteTransaction(transaction);

        return "redirect:/profile/operations";
    }


    @PatchMapping
    public String editTransaction(@ModelAttribute("transaction") @Valid Transaction transaction,
                                 BindingResult bindingResult, @AuthenticationPrincipal MyUserDetails myUserDetails){

        if(transaction.getTypeOfTransfer().toString().equals("EXPENSE")){
            transaction.setIncomeSource(null);
        }
        else if(transaction.getTypeOfTransfer().toString().equals("INCOME")){
            transaction.setExpenseCategory(null);
        }

        User user = myUserDetails.getUser();
        transaction.setUser(user);

        transactionService.createOrUpdateTransaction(transaction);
        return "redirect:/profile/operations";
    }
}
