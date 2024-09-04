package ru.Tim.Proj.moneyAnalyzer.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.Tim.Proj.moneyAnalyzer.Config.MyUserDetails;
import ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Category.ExpenseCategoryService;
import ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Category.IncomeSourceService;
import ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Other.TransactionService;
import ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Plans.*;
import org.springframework.stereotype.Controller;
import ru.Tim.Proj.moneyAnalyzer.Models.Category.ExpenseCategory;
import ru.Tim.Proj.moneyAnalyzer.Models.Category.IncomeSource;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.Transaction;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;
import ru.Tim.Proj.moneyAnalyzer.Models.Plan.PlannedExpense;
import ru.Tim.Proj.moneyAnalyzer.Models.Plan.PlannedIncome;

import java.math.BigDecimal;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.zip.DataFormatException;


@RequestMapping("/profile/analytics")
@Controller
public class AnalyticsController {

    private final ePlannedService ePlannedService;
    private final iPlannedService iPlannedService;
    private final ExpenseCategoryService expenseCategoryService;
    private final IncomeSourceService incomeSourceService;
    private final TransactionService transactionService;

    @Autowired
    public AnalyticsController(ePlannedService ePlannedService,
                               iPlannedService iPlannedService,
                               ExpenseCategoryService expenseCategoryService,
                               IncomeSourceService incomeSourceService,
                               TransactionService transactionService) {
        this.ePlannedService = ePlannedService;
        this.iPlannedService = iPlannedService;
        this.incomeSourceService = incomeSourceService;
        this.expenseCategoryService = expenseCategoryService;
        this.transactionService = transactionService;
    }

    @GetMapping
    public String plansHomePage(Model model){
        return "analyticspages/analytics";
    }

    @GetMapping("/plans")
    public String plansPage(@AuthenticationPrincipal MyUserDetails myUserDetails,
                            Model model, @RequestParam(name = "date", required = false) String date){


        if(date == null || date.isEmpty()){
            date = YearMonth.now().toString();
        }

        YearMonth currentDate;
        try{
            currentDate = YearMonth.parse(date);
        } catch (DateTimeParseException e) {
            currentDate = YearMonth.now();
            date = currentDate.toString();
        }

        YearMonth prevMonthDate = currentDate.minusMonths(1);
        YearMonth nextMonthDate = currentDate.plusMonths(1);

        Long id = myUserDetails.getUser().getId();

        List<PlannedIncome> plannedIncList = iPlannedService.getPlannedIncList(id, date);
        List<PlannedExpense> plannedExpList = ePlannedService.getPlannedExpList(id, date);
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        BigDecimal freeSum = BigDecimal.ZERO;
        for(PlannedIncome plannedIncome : plannedIncList){
            totalIncome = totalIncome.add(plannedIncome.getAmount());
        }
        for(PlannedExpense plannedExpense : plannedExpList){
            totalExpense = totalExpense.add(plannedExpense.getAmount());
        }
        freeSum = freeSum.add(totalIncome).subtract(totalExpense);

        List<IncomeSource> sourceList = incomeSourceService.getCategoryList(id);
        Set<Long> usedIncIds = plannedIncList.stream()
                .map(income -> income.getIncomeSource().getId())
                .collect(Collectors.toSet());
        List<IncomeSource> availableIncomeSources = sourceList.stream()
                .filter(source -> !usedIncIds.contains(source.getId()))
                .toList();

        List<ExpenseCategory> categoryList = expenseCategoryService.getCategoryList(id);
        Set<Long> usedExpIds = plannedExpList.stream()
                .map(expense -> expense.getExpenseCategory().getId())
                .collect(Collectors.toSet());
        List<ExpenseCategory> availableExpenseCategory = categoryList.stream()
                .filter(expense -> !usedExpIds.contains(expense.getId()))
                .toList();

        Map<Long, BigDecimal> categoryTransSum = transactionService.getTotalAmountsExp(id, date);
        Map<Long, BigDecimal> sourceTransSum = transactionService.getTotalAmountsInc(id, date);
        BigDecimal totalTransExp = categoryTransSum.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTransInc = sourceTransSum.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal factFreeSum = BigDecimal.ZERO;
        factFreeSum = factFreeSum.add(totalTransInc).subtract(totalTransExp);

        model.addAttribute("plannedExpList", plannedExpList);
        model.addAttribute("plannedIncList", plannedIncList);
        model.addAttribute("expenseCategories", availableExpenseCategory);
        model.addAttribute("incomeSources", availableIncomeSources);
        model.addAttribute("categoryTransSum", categoryTransSum);
        model.addAttribute("sourceTransSum", sourceTransSum);
        model.addAttribute("totalExp", totalExpense);
        model.addAttribute("totalInc", totalIncome);
        model.addAttribute("freeSum", freeSum);
        model.addAttribute("totalTransExp", totalTransExp);
        model.addAttribute("totalTransInc", totalTransInc);
        model.addAttribute("factFreeSum", factFreeSum);
        model.addAttribute("curDate", date);
        model.addAttribute("prevMonth", prevMonthDate);
        model.addAttribute("nextMonth", nextMonthDate);

        return "analyticspages/plans";
    }

    @GetMapping("/compare")
    public String compareExpense(@AuthenticationPrincipal MyUserDetails myUserDetails,
                                 Model model, @RequestParam(value = "firstDate", required = false) String firstDate,
                                 @RequestParam(value = "secondDate", required = false) String secondDate){


        if(firstDate == null || firstDate.isEmpty()){
            YearMonth cur = YearMonth.now();
            YearMonth subCur = cur.minusMonths(1);
            firstDate = cur.toString();
            secondDate = subCur.toString();
        }

        try{
            YearMonth.parse(firstDate);
            YearMonth.parse(secondDate);
        } catch (DateTimeParseException e){
            firstDate = YearMonth.now().toString();
            secondDate = YearMonth.now().minusMonths(1).toString();
        }

        Long id = myUserDetails.getUser().getId();

        Map<Long, BigDecimal> AmountsByCategoryFirstDate = transactionService.getTotalAmountsExp(id, firstDate);
        Map<Long, BigDecimal> AmountsByCategorySecondDate = transactionService.getTotalAmountsExp(id, secondDate);

        BigDecimal firstDateTotal = AmountsByCategoryFirstDate.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal secondDateTotal = AmountsByCategorySecondDate.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalDifference = BigDecimal.ZERO;
        totalDifference = totalDifference.add(firstDateTotal);
        totalDifference = totalDifference.subtract(secondDateTotal);

        Map<Long, BigDecimal> categoryDifference = new HashMap<>(AmountsByCategoryFirstDate);
        for(Map.Entry<Long, BigDecimal> entry : AmountsByCategorySecondDate.entrySet()){
            Long categoryId = entry.getKey();
            BigDecimal amount = entry.getValue();
            categoryDifference.merge(categoryId, amount.negate(), BigDecimal::add);
        }

        model.addAttribute("expenseCategories", expenseCategoryService.getCategoryList(id));
        model.addAttribute("incomeSources", incomeSourceService.getCategoryList(id));
        model.addAttribute("firstDateTotal", firstDateTotal);
        model.addAttribute("secondDateTotal", secondDateTotal);
        model.addAttribute("totalDifference", totalDifference);
        model.addAttribute("categoryDifference", categoryDifference);
        model.addAttribute("firstDate", firstDate);
        model.addAttribute("secondDate", secondDate);

        return "analyticspages/compare";
    }

    @GetMapping("/categoryChart")
    public String categoryExp(@AuthenticationPrincipal MyUserDetails myUserDetails,
                              Model model, @RequestParam(name = "date", required = false) String date,
                              @RequestParam(name="expOrInc") String expOrInc){
        if(date == null || date.isEmpty()){
            YearMonth now = YearMonth.now();
            date = now.toString();
        }

        YearMonth currentDate;
        try{
            currentDate = YearMonth.parse(date);
        } catch (DateTimeParseException e) {
            currentDate = YearMonth.now();
        }

        YearMonth prevMonthDate = currentDate.minusMonths(1);
        YearMonth nextMonthDate = currentDate.plusMonths(1);

        Long id = myUserDetails.getUser().getId();

        Map<Long, BigDecimal> categoryTransSum = null;
        Map<Long, BigDecimal> IncTotalAmounts = transactionService.getTotalAmountsInc(id, date);
        Map<Long, BigDecimal> ExpTotalAmounts = transactionService.getTotalAmountsExp(id, date);
        List<String> categoryNames = null;
        List<IncomeSource> incSources = incomeSourceService.getCategoryList(id);
        List<ExpenseCategory> expCategories = expenseCategoryService.getCategoryList(id);
        if(expOrInc.equals("INC") && (!IncTotalAmounts.isEmpty())){
            categoryTransSum = IncTotalAmounts;
            categoryNames = categoryTransSum.keySet().stream()
                    .map(key -> incSources.stream()
                            .filter(source -> source.getId().equals(key))
                            .findFirst()
                            .map(IncomeSource::getSourceName)
                            .orElse("Unknown"))
                    .collect(Collectors.toList());
        }
        else if(expOrInc.equals("EXP") && !ExpTotalAmounts.isEmpty()){
            categoryTransSum = ExpTotalAmounts;
            categoryNames = categoryTransSum.keySet().stream()
                    .map(key -> expCategories.stream()
                            .filter(category -> category.getId().equals(key))
                            .findFirst()
                            .map(ExpenseCategory::getCategoryName)
                            .orElse("Unknown"))
                    .collect(Collectors.toList());
        }
        model.addAttribute("categoryMap", categoryTransSum);
        model.addAttribute("categoryNames", categoryNames);
        model.addAttribute("curDate", date);
        model.addAttribute("prevMonth", prevMonthDate);
        model.addAttribute("nextMonth", nextMonthDate);
        model.addAttribute("expOrInc", expOrInc);

        return "analyticspages/categoryChart";
    }

    @GetMapping("/yearTrend")
    public String getYearExpTrend(@AuthenticationPrincipal MyUserDetails myUserDetails,
                                  Model model, @RequestParam(name = "date", required = false) String date){

        if(date == null || date.isEmpty()){
            date = Year.now().toString();
        }

        try{
            Year.parse(date);
        }catch (DateTimeParseException e){
            date = Year.now().toString();
        }


        Long id = myUserDetails.getUser().getId();

        Year currentDate = Year.parse(date);
        Year prevYear = currentDate.minusYears(1);
        Year nextYear = currentDate.plusYears(1);

        model.addAttribute("curYear", date);
        model.addAttribute("prevYear", prevYear);
        model.addAttribute("nextYear", nextYear);
        model.addAttribute("monthAmounts", transactionService.getYearMonthAmounts(id, date));

        return "analyticspages/yearExpChart";
    }

    @GetMapping("/balanceDynamic")
    public String getBalanceDynamic(@AuthenticationPrincipal MyUserDetails myUserDetails,
                                    Model model, @RequestParam(name = "date", required = false) String date){
        Long id = myUserDetails.getUser().getId();

        if(date == null || date.isEmpty()){
            YearMonth now = YearMonth.now();
            date = now.toString();
        }

        try{
            YearMonth.parse(date);
        }catch (DateTimeParseException e){
            date = YearMonth.now().toString();
        }

        YearMonth currentDate = YearMonth.parse(date);
        YearMonth prevMonthDate = currentDate.minusMonths(1);
        YearMonth nextMonthDate = currentDate.plusMonths(1);

        model.addAttribute("curDate", date);
        model.addAttribute("prevMonth", prevMonthDate);
        model.addAttribute("nextMonth", nextMonthDate);
        model.addAttribute("cumulativeAmount",
                transactionService.getMonthAmountGrowth(id, date, Transaction.TransferType.EXPENSE));
        model.addAttribute("cumulativeIncomeAmount",
                transactionService.getMonthAmountGrowth(id, date, Transaction.TransferType.INCOME));
        return "analyticspages/balanceDynamic";
    }

    @PostMapping("/plans/addInc")
    public String addIncPlan(@ModelAttribute PlannedIncome plannedIncome,
                             @AuthenticationPrincipal MyUserDetails myUserDetails,
                             @RequestParam(name = "planDate") String date){

        plannedIncome.setYearMonth(date);

        User user = myUserDetails.getUser();
        plannedIncome.setUser(user);

        iPlannedService.addIncomePlan(plannedIncome);

        return "redirect:/profile/analytics/plans?date=" + date;
    }

    @PostMapping("/plans/addExp")
    public String addExpPlan(@ModelAttribute PlannedExpense plannedExpense,
                             @AuthenticationPrincipal MyUserDetails myUserDetails,
                             @RequestParam(name = "planDate") String date){

        plannedExpense.setYearMonth(date);

        User user = myUserDetails.getUser();
        plannedExpense.setUser(user);

        ePlannedService.addPlannedExpense(plannedExpense);

        return "redirect:/profile/analytics/plans?date=" + date;
    }

    @PatchMapping("/plans/editExp")
    public String editExpPlan(@ModelAttribute PlannedExpense plannedExpense,
                              @AuthenticationPrincipal MyUserDetails myUserDetails,
                              @RequestParam(name = "planDate") String date){
        User user = myUserDetails.getUser();
        plannedExpense.setUser(user);
        plannedExpense.setYearMonth(date);
        ePlannedService.addPlannedExpense(plannedExpense);
        return "redirect:/profile/analytics/plans?date=" + date;
    }

    @DeleteMapping("/plans/deleteExp")
    public String deleteExpPlan(@RequestParam("id") Long id,
                                @RequestParam(name = "planDate") String date){
        ePlannedService.deletePlannedExpense(id);
        return "redirect:/profile/analytics/plans?date=" + date;
    }

    @PatchMapping("/plans/editInc")
    public String editIncPlan(@ModelAttribute PlannedIncome plannedIncome,
                              @AuthenticationPrincipal MyUserDetails myUserDetails,
                              @RequestParam(name = "planDate") String date){
        User user = myUserDetails.getUser();
        plannedIncome.setUser(user);
        plannedIncome.setYearMonth(date);
        iPlannedService.addIncomePlan(plannedIncome);
        return "redirect:/profile/analytics/plans?date=" + date;
    }

    @DeleteMapping("/plans/deleteInc")
    public String deleteIncPlan(@RequestParam("id") Long id,
                                @RequestParam(name = "planDate") String date){
        iPlannedService.deleteIncomePlan(id);
        return "redirect:/profile/analytics/plans?date=" + date;
    }

}
