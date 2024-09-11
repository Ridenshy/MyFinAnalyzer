package ru.Tim.Proj.moneyAnalyzer.Services;


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.Tim.Proj.moneyAnalyzer.Models.Category.ExpenseCategory;
import ru.Tim.Proj.moneyAnalyzer.Models.Category.IncomeSource;
import ru.Tim.Proj.moneyAnalyzer.Models.HolderModels.*;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.Transaction;
import ru.Tim.Proj.moneyAnalyzer.Models.Other.User;
import ru.Tim.Proj.moneyAnalyzer.Models.Plan.PlannedExpense;
import ru.Tim.Proj.moneyAnalyzer.Models.Plan.PlannedIncome;
import ru.Tim.Proj.moneyAnalyzer.Repositoryes.Category.ExpenseRepository;
import ru.Tim.Proj.moneyAnalyzer.Repositoryes.Category.IncomeRepository;
import ru.Tim.Proj.moneyAnalyzer.Repositoryes.Other.HolderRepository;
import ru.Tim.Proj.moneyAnalyzer.Repositoryes.Other.TransactionRepository;
import ru.Tim.Proj.moneyAnalyzer.Repositoryes.Plans.EPlanRepository;
import ru.Tim.Proj.moneyAnalyzer.Repositoryes.Plans.IPlanRepository;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExcelService {

    private final TransactionRepository transactionRepository;
    private final HolderRepository holderRepository;
    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final EPlanRepository ePlanRepository;
    private final IPlanRepository iPlanRepository;

    public ExcelService(TransactionRepository transactionRepository,
                        HolderRepository holderRepository,
                        ExpenseRepository expenseRepository,
                        IncomeRepository incomeRepository,
                        EPlanRepository ePlanRepository,
                        IPlanRepository iPlanRepository) {
        this.transactionRepository = transactionRepository;
        this.holderRepository = holderRepository;
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.ePlanRepository = ePlanRepository;
        this.iPlanRepository = iPlanRepository;
    }


    public byte[] generateUserDataExcel(User user) throws IOException{
        try(Workbook workbook = new XSSFWorkbook()){
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Sheet instructionSheet = workbook.createSheet("Пример ручного заполнения");
            createInstructionSheet(instructionSheet, workbook);
            Sheet holdersSheet = workbook.createSheet("Счета");
            createHoldersSheet(holdersSheet, user, workbook);
            Sheet categorySheet = workbook.createSheet("Категории расхода");
            createCategorySheet(categorySheet, user, workbook);
            Sheet sourceSheet = workbook.createSheet("Источники дохода");
            createSourceSheet(sourceSheet, user, workbook);
            Sheet transactionSheet = workbook.createSheet("Операции");
            createTransactionSheet(transactionSheet, user, workbook);
            Sheet plannedIncomeSheet = workbook.createSheet("Планы расходов");
            createPlannedIncSheet(plannedIncomeSheet, user, workbook);
            Sheet plannedExpenseSheet = workbook.createSheet("Планы дохода");
            createPlannedExpSheet(plannedExpenseSheet, user, workbook);

            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    public void importUserDataExcel(User user, MultipartFile multipartFile) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(multipartFile.getInputStream())) {
            parseExcelToHolders(user, workbook);
            parseExcelToCategory(user, workbook);
            parseExcelToTransactions(user, workbook);
            parseExcelToPlans(user, workbook);
        }
    }

    private void parseExcelToHolders(User user, Workbook workbook){
        Map<String, DepositAccount> depositAccountMap = new HashMap<>();
        for(int rowIndex = 1; rowIndex <= workbook.getSheet("Счета").getLastRowNum(); rowIndex++){
            Row row = workbook.getSheet("Счета").getRow(rowIndex);
            if(row == null){continue;}
            MoneyHolders holder = null;
            String holderType = row.getCell(0).getStringCellValue();
            switch (holderType) {
                case "BankAccount" -> holder = new BankAccount();
                case "CashAccount" -> holder = new CashAccount();
                case "SavingsAccount" -> holder = new SavingsAccount();
                case "DepositAccount" -> holder = new DepositAccount();
            }
            holder.setHolderName(row.getCell(1).getStringCellValue());
            holder.setAmount(BigDecimal.valueOf(row.getCell(2).getNumericCellValue()));
            holder.setUser(user);
            holder.setActiveCheck(false);
            if(holder instanceof BankAccount bank){
                bank.setCreditLimit(BigDecimal.valueOf(row.getCell(3).getNumericCellValue()));
            } else if(holder instanceof SavingsAccount savings){
                savings.setActiveCheck(true);
                savings.setInterestRate(BigDecimal.valueOf(row.getCell(6).getNumericCellValue()));
                savings.setOpenDate(LocalDate
                        .parse(row.getCell(7).getStringCellValue().replace("\"","")));
                savings.setNextInterestDate(savings.getOpenDate());
                savings.calcWhileNormalDate();
            } else if(holder instanceof DepositAccount deposit){
                deposit.setActiveCheck(true);
                deposit.setCapitalization(row.getCell(4).getStringCellValue().equals("+"));
                deposit.setInterestPeriod(DepositAccount.InterestPeriod.valueOf(row.getCell(5).getStringCellValue().toUpperCase()));
                deposit.setInterestRate(BigDecimal.valueOf(row.getCell(6).getNumericCellValue()));
                deposit.setOpenDate(LocalDate
                        .parse(row.getCell(7).getStringCellValue().replace("\"","")));
                long term = (long) row.getCell(8).getNumericCellValue();
                deposit.setTermMonths(term);
                deposit.setNextInterestDate(deposit.calculateNextInterestDate(deposit.getOpenDate()));
                deposit.calcWhileNormalDate();
                depositAccountMap.put(row.getCell(9).getStringCellValue(), deposit);
            }
            holderRepository.save(holder);
        }
        for(Map.Entry<String, DepositAccount> entry : depositAccountMap.entrySet()){
            MoneyHolders holder = holderRepository.findByHolderNameAndUser(entry.getKey(), user);
            entry.getValue().setHolder(holder);
            holderRepository.save(entry.getValue());
        }
    }

    private void parseExcelToCategory(User user, Workbook workbook){
        for(int rowIndex = 1; rowIndex <= workbook.getSheet("Категории расхода").getLastRowNum(); rowIndex++) {
            Row row = workbook.getSheet("Категории расхода").getRow(rowIndex);
            if (row == null) {continue;}
            ExpenseCategory category = new ExpenseCategory();
            if(!row.getCell(1).getStringCellValue().equals("Базовая категория")){
                category.setCategoryName(row.getCell(0).getStringCellValue());
                category.setUser(user);
                expenseRepository.save(category);
            }
        }
        for(int rowIndex = 1; rowIndex <= workbook.getSheet("Источники дохода").getLastRowNum(); rowIndex++){
            Row row = workbook.getSheet("Источники дохода").getRow(rowIndex);
            if (row == null) {continue;}
            IncomeSource source = new IncomeSource();
            if(!row.getCell(1).getStringCellValue().equals("Базовый источник")){
                source.setSourceName(row.getCell(0).getStringCellValue());
                source.setUser(user);
                incomeRepository.save(source);
            }
        }
    }

    private void parseExcelToTransactions(User user, Workbook workbook){
        for(int rowIndex = 1; rowIndex <= workbook.getSheet("Операции").getLastRowNum(); rowIndex++) {
            Row row = workbook.getSheet("Операции").getRow(rowIndex);
            if (row == null) {continue;}
            Transaction transaction = new Transaction();
            transaction.setAmount(BigDecimal.valueOf(row.getCell(0).getNumericCellValue()));
            transaction.setTransComment(row.getCell(1).getStringCellValue());
            transaction.setTransactionDate(LocalDate
                    .parse(row.getCell(2).getStringCellValue().replace("\"","")));
            transaction.setTypeOfTransfer(Transaction.TransferType
                    .valueOf(row.getCell(3).getStringCellValue().toUpperCase()));
            if(transaction.getTypeOfTransfer().toString().equals("INCOME")){
                transaction.setIncomeSource(incomeRepository.findBySourceNameAndUser(
                        row.getCell(5).getStringCellValue(), user));
            }else if(transaction.getTypeOfTransfer().toString().equals("EXPENSE")){
                transaction.setExpenseCategory(expenseRepository.findByCategoryNameAndUser(
                        row.getCell(4).getStringCellValue(), user));
            }
            transaction.setMoneyHolders(holderRepository
                    .findByHolderNameAndUser(row.getCell(6).getStringCellValue(), user));
            transaction.setUser(user);
            transactionRepository.save(transaction);
        }
    }

    private void parseExcelToPlans(User user, Workbook workbook){
        for(int rowIndex = 1; rowIndex <= workbook.getSheet("Планы расходов").getLastRowNum(); rowIndex++){
            Row row = workbook.getSheet("Планы расходов").getRow(rowIndex);
            if(row == null) {continue;}
            PlannedExpense plannedExpense = new PlannedExpense();
            plannedExpense.setAmount(BigDecimal.valueOf(row.getCell(0).getNumericCellValue()));
            plannedExpense.setYearMonth(row.getCell(1).getStringCellValue());
            plannedExpense.setExpenseCategory(expenseRepository.findByCategoryNameAndUser(
                    row.getCell(2).getStringCellValue(), user));
            plannedExpense.setUser(user);
            ePlanRepository.save(plannedExpense);
        }
        for(int rowIndex = 1; rowIndex <= workbook.getSheet("Планы дохода").getLastRowNum(); rowIndex++) {
            Row row = workbook.getSheet("Планы дохода").getRow(rowIndex);
            if(row == null){continue;}
            PlannedIncome plannedIncome = new PlannedIncome();
            plannedIncome.setAmount(BigDecimal.valueOf(row.getCell(0).getNumericCellValue()));
            plannedIncome.setYearMonth(row.getCell(1).getStringCellValue());
            plannedIncome.setIncomeSource(incomeRepository.findBySourceNameAndUser(
                    row.getCell(2).getStringCellValue(), user));
            plannedIncome.setUser(user);
            iPlanRepository.save(plannedIncome);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.BLACK.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setAlignment(HorizontalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBottomBorderColor(IndexedColors.GREY_50_PERCENT.getIndex());

        return style;
    }

    private CellStyle createCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();

        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);

        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        return style;
    }

    private int createHeaderRow(Sheet sheet, int rowIndex, String[] headers, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(rowIndex++);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
            sheet.autoSizeColumn(i);
        }
        return rowIndex;
    }

    public void createHoldersSheet(Sheet sheet, User user, Workbook workbook){
        List<MoneyHolders> holders = holderRepository.findAllByUser(user);
        int rowIndex = 0;

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle cellStyle = createCellStyle(workbook);

        String[] headers = {
                "Тип счета", "Название счета", "Сумма", "Кредитный лимит(если банк. счет)",
                "Капитализация (если депозит)", "Период начисления % (если депозит)",
                "Процентная ставка (депозит/накоп.)", "Дата открытия (депозит/накоп.)",
                "Срок в месяцах (если депозит)", "Имя счета перевода (если депозит)"
        };

        rowIndex = createHeaderRow(sheet, rowIndex, headers, headerStyle);

        for(MoneyHolders holder : holders){
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(holder.getClass().getSimpleName());
            row.createCell(1).setCellValue(holder.getHolderName());
            row.createCell(2).setCellValue(holder.getAmount().doubleValue());
            if(holder instanceof BankAccount bankAccount){
                row.createCell(3).setCellValue(bankAccount.getCreditLimit().doubleValue());
            }
            if(holder instanceof DepositAccount deposit){
                row.createCell(4).setCellValue(deposit.isCapitalization() ? "+" : "-");
                row.createCell(5).setCellValue(deposit.getInterestPeriod().toString());
                row.createCell(8).setCellValue(deposit.getTermMonths());
                row.createCell(9).setCellValue(deposit.getHolder().getHolderName());

                row.createCell(6).setCellValue(deposit.getInterestRate().doubleValue());
                row.createCell(7).setCellValue("\"" + deposit.getOpenDate().toString() + "\"");
            }
            else if(holder instanceof SavingsAccount savings){
                row.createCell(6).setCellValue(savings.getInterestRate().doubleValue());
                row.createCell(7).setCellValue("\"" + savings.getOpenDate().toString() + "\"");
            }

            for (int i = 0; i < 10; i++) {
                if(row.getCell(i) == null){ row.createCell(i); }
                row.getCell(i).setCellStyle(cellStyle);
            }
        }

        for (int i = 0; i < 10; i++) { sheet.autoSizeColumn(i); }
    }

    public void createTransactionSheet(Sheet sheet, User user, Workbook workbook){
        List<Transaction> transactions = transactionRepository.findByUserId(user.getId());
        int rowIndex = 0;

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle cellStyle = createCellStyle(workbook);

        String[] headers = {
                "Сумма", "Комментарий",  "Дата",  "Тип операции",
                "Категория трат (если трата)", "Источник дохода (если доход)",
                "Имя счета"
        };

        rowIndex = createHeaderRow(sheet, rowIndex, headers, headerStyle);

        for(Transaction transaction : transactions){
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(transaction.getAmount().doubleValue());
            row.createCell(1).setCellValue(transaction.getTransComment());
            row.createCell(2).setCellValue("\"" + transaction.getTransactionDate().toString() + "\"");
            row.createCell(3).setCellValue(transaction.getTypeOfTransfer().toString());
            if(transaction.getTypeOfTransfer().toString().equals("INCOME")){
                row.createCell(5).setCellValue(transaction.getIncomeSource().getSourceName());
            }else{
                row.createCell(4).setCellValue(transaction.getExpenseCategory().getCategoryName());
            }
            row.createCell(6).setCellValue(transaction.getMoneyHolders().getHolderName());

            for (int i = 0; i < 7; i++) {
                if(row.getCell(i) == null){ row.createCell(i); }
                row.getCell(i).setCellStyle(cellStyle);
            }
        }

        for (int i = 0; i < 7; i++) { sheet.autoSizeColumn(i); }
    }

    public void createCategorySheet(Sheet sheet, User user, Workbook workbook){
        List<ExpenseCategory> categoryList = expenseRepository.getCategories(user.getId());
        int rowIndex = 0;

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle cellStyle = createCellStyle(workbook);

        String[] headers = {"Название категории", "Тип категории"};

        rowIndex = createHeaderRow(sheet, rowIndex, headers, headerStyle);

        for(ExpenseCategory expenseCategory : categoryList){
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(expenseCategory.getCategoryName());
            if(expenseCategory.getUser() == null){
                row.createCell(1).setCellValue("Базовая категория");
            }else{
                row.createCell(1).setCellValue("Пользовательская категория");
            }
            for (int i = 0; i < 2; i++) {
                if(row.getCell(i) == null){ row.createCell(i); }
                row.getCell(i).setCellStyle(cellStyle);
            }
        }

        for (int i = 0; i < 2; i++) { sheet.autoSizeColumn(i); }
    }

    public void createSourceSheet(Sheet sheet, User user, Workbook workbook){
        List<IncomeSource> sourceList = incomeRepository.getSources(user.getId());
        int rowIndex = 0;

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle cellStyle = createCellStyle(workbook);

        String[] headers = {"Название категории", "Тип категории"};

        rowIndex = createHeaderRow(sheet, rowIndex, headers, headerStyle);

        for(IncomeSource incomeSource : sourceList){
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(incomeSource.getSourceName());
            if(incomeSource.getUser() == null){
                row.createCell(1).setCellValue("Базовый источник");
            }else{
                row.createCell(1).setCellValue("Пользовательский источник");
            }
            for (int i = 0; i < 2; i++) {
                if(row.getCell(i) == null){ row.createCell(i); }
                row.getCell(i).setCellStyle(cellStyle);
            }
        }

        for (int i = 0; i < 2; i++) { sheet.autoSizeColumn(i); }
    }

    public void createPlannedIncSheet(Sheet sheet, User user, Workbook workbook){
        List<PlannedExpense> plannedExpenseList = ePlanRepository.findAllByUser(user);
        int rowIndex = 0;

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle cellStyle = createCellStyle(workbook);


        String[] headers = {"Сумма", "Год-месяц", "Категория"};

        rowIndex = createHeaderRow(sheet, rowIndex, headers, headerStyle);

        for(PlannedExpense plannedExpense : plannedExpenseList){
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(plannedExpense.getAmount().doubleValue());
            row.createCell(1).setCellValue(plannedExpense.getYearMonth());
            row.createCell(2).setCellValue(plannedExpense.getExpenseCategory().getCategoryName());

            for (int i = 0; i < 3; i++) {
                if(row.getCell(i) == null) { row.createCell(i); }
                row.getCell(i).setCellStyle(cellStyle);
            }
        }

        for (int i = 0; i < 3; i++) { sheet.autoSizeColumn(i); }
    }

    public void createPlannedExpSheet(Sheet sheet, User user, Workbook workbook){
        List<PlannedIncome> plannedIncomeList = iPlanRepository.findAllByUser(user);
        int rowIndex = 0;

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle cellStyle = createCellStyle(workbook);

        String[] headers = { "Сумма", "Год-месяц", "Категория" };

        rowIndex = createHeaderRow(sheet, rowIndex, headers, headerStyle);

        for(PlannedIncome plannedIncome : plannedIncomeList){
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(plannedIncome.getAmount().doubleValue());
            row.createCell(1).setCellValue(plannedIncome.getYearMonth());
            row.createCell(2).setCellValue(plannedIncome.getIncomeSource().getSourceName());

            for (int i = 0; i < 3; i++) {
                if(row.getCell(i) == null){ row.createCell(i); }
                row.getCell(i).setCellStyle(cellStyle);
            }
        }
        for (int i = 0; i < 3; i++) { sheet.autoSizeColumn(i); }
    }

    public void deleteOldData(User user){
        transactionRepository.deleteAllByUser(user);
        ePlanRepository.deleteAllByUser(user);
        iPlanRepository.deleteAllByUser(user);
        expenseRepository.deleteAllByUser(user);
        incomeRepository.deleteAllByUser(user);
        holderRepository.deleteAllByUser(user);

    }

    public void createInstructionSheet(Sheet sheet, Workbook workbook) {
        int rowIndex = 0;

        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle cellStyle = createCellStyle(workbook);

        rowIndex = createSectionHeader(sheet, rowIndex, "Пример заполнения таблицы счетов (копировать строку для добавления данных)");

        String[] headers = {
                "Тип счета", "Название счета", "Сумма", "Кредитный лимит(если банк. счет)",
                "Капитализация (если депозит)", "Период начисления % (если депозит)",
                "Процентная ставка (депозит/накоп.)", "Дата открытия (депозит/накоп.)",
                "Срок в месяцах (если депозит)", "Имя счета перевода (если депозит)"
        };

        rowIndex = createHeaderRow(sheet, rowIndex, headers, headerStyle);
        String[] accountTypes = {"CashAccount", "BankAccount", "SavingsAccount", "DepositAccount"};
        for (String accountType : accountTypes) {
            Row row = sheet.createRow(rowIndex++);
            fillHoldersRow(row, accountType, cellStyle);
        }

        rowIndex = createSectionHeader(sheet, rowIndex, "Пример заполнения транзакций (копировать строку для добавления данных)");
        String[] transactionHeaders = {
                "Сумма", "Комментарий", "Дата", "Тип операции", "Категория трат (если трата)",
                "Источник дохода (если доход)", "Имя счета"
        };
        rowIndex = createHeaderRow(sheet, rowIndex, transactionHeaders, headerStyle);

        Row expenseRow = sheet.createRow(rowIndex++);
        fillTransactionRow(expenseRow, "EXPENSE",  cellStyle);
        Row incomeRow = sheet.createRow(rowIndex++);
        fillTransactionRow(incomeRow, "INCOME", cellStyle);

        rowIndex = createSectionHeader(sheet, rowIndex, "Пример заполнения категорий и планов (копировать строку для добавления данных)");
        String[] OtherHeaders = {"Название категории", "Тип категории", "|", "Сумма", "Год-месяц", "Категория"};
        rowIndex = createHeaderRow(sheet, rowIndex, OtherHeaders, headerStyle);
        Row otherRow = sheet.createRow(rowIndex);
        fillOtherRow(otherRow, cellStyle);
        for(int i = 0; i < 11; i++){sheet.autoSizeColumn(i);}
    }

    private int createSectionHeader(Sheet sheet, int rowIndex, String headerText) {
        Row headerRow = sheet.createRow(rowIndex++);
        headerRow.createCell(0).setCellValue(headerText);
        rowIndex++;
        return rowIndex;
    }


    private void fillHoldersRow(Row row, String accountType, CellStyle cellStyle) {
        row.createCell(0).setCellValue(accountType);
        Cell firstCell = row.getCell(0);
        firstCell.setCellStyle(cellStyle);

        for (int i = 1; i <= 9; i++) {
            Cell cell = row.createCell(i);
            cell.setCellStyle(cellStyle);
            if (i < 3) cell.setCellValue("?");
            switch (accountType) {
                case "CashAccount", "BankAccount" -> {
                    if ("BankAccount".equals(accountType) && i == 3) {
                        cell.setCellValue("?");
                    }
                }
                case "SavingsAccount" -> {
                    if (i == 6 || i == 7) cell.setCellValue(i == 6 ? "?" : "\"yyyy-mm-dd\"");
                }
                case "DepositAccount" -> {
                    if (i == 4 || i == 5){
                        cell.setCellValue(i == 4 ? "+/-" : "MONTHLY, QUARTERLY, YEARLY, END_OF_TERM");
                    }
                    if (i > 5) cell.setCellValue(i == 7 ? "\"yyyy-mm-dd\"": "?");
                }
            }
        }
    }

    private void fillTransactionRow(Row row, String transactionType, CellStyle cellStyle) {
        String[] transactionData = new String[7];
        transactionData[0] = "?";
        transactionData[1] = "Пример коммент.";
        transactionData[2] = "\"yyyy-mm-dd\"";
        transactionData[3] = transactionType;

        if ("EXPENSE".equals(transactionType)) {
            transactionData[4] = "?";
        } else if ("INCOME".equals(transactionType)) {
            transactionData[5] = "?";
        }
        transactionData[6] = "?";
        for (int i = 0; i < transactionData.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(transactionData[i] != null ? transactionData[i] : "");
            cell.setCellStyle(cellStyle);
        }
    }

    private void fillOtherRow(Row row, CellStyle cellStyle){
        row.createCell(0).setCellValue("Имя категории");
        row.createCell(1).setCellValue("Пользовательский источник");
        row.createCell(3).setCellValue("?");
        row.createCell(4).setCellValue("\"yyyy-mm\"");
        row.createCell(5).setCellValue("Имя катеогрии");
        for (int i = 0; i < 6; i++) {
            if(row.getCell(i) == null){ row.createCell(i); }
            row.getCell(i).setCellStyle(cellStyle);
        }
    }
}
