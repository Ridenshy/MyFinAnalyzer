package ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Category;


import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import ru.Tim.Proj.moneyAnalyzer.Models.Category.ExpenseCategory;
import ru.Tim.Proj.moneyAnalyzer.Repositoryes.Category.ExpenseRepository;

import java.util.List;

@Service
public class ExpenseCategoryService {

    private final ExpenseRepository expenseRepository;


    public ExpenseCategoryService(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @PostConstruct
    public void checkStart(){
        Integer amount = checkFirstStart();
        if(amount.equals(0)){
            insertDefaultCategories();
            System.out.println("Категории созданы успешно");
        }else {
            System.out.println(amount + ": Количество категорий");
        }
    }

    public Integer checkFirstStart(){
        return expenseRepository.getCategoryAmount();
    }

    private void insertDefaultCategories() {
        List<ExpenseCategory> defaultCategories = List.of(
                new ExpenseCategory("Продукты", null),
                new ExpenseCategory("Комуналка", null),
                new ExpenseCategory("Кредитные платежи", null),
                new ExpenseCategory("Аренда", null),
                new ExpenseCategory("Связь", null),
                new ExpenseCategory("Здоровье", null),
                new ExpenseCategory("Образование", null),
                new ExpenseCategory("Вещи для дома", null),
                new ExpenseCategory("Одежда", null),
                new ExpenseCategory("Техника", null),
                new ExpenseCategory("Штрафы", null),
                new ExpenseCategory("Подарки", null),
                new ExpenseCategory("Подписки", null),
                new ExpenseCategory("Путешествия", null),
                new ExpenseCategory("Разное", null),
                new ExpenseCategory("Кафе и рестораны", null),
                new ExpenseCategory("Дети", null),
                new ExpenseCategory("Косметика", null)
        );

        for (ExpenseCategory category : defaultCategories) {
            expenseRepository.save(category);
        }
    }

    public void createOrUpdate(ExpenseCategory expenseCategory){ expenseRepository.save(expenseCategory); }

    public List<ExpenseCategory> getCategoryList(Long id){
        return expenseRepository.getCategories(id);
    }

    public List<ExpenseCategory> getUserCategoryList(Long id) { return expenseRepository.getOnlyUserCategories(id); }

    public ExpenseCategory findCategoryById(Long id) { return expenseRepository.findById(id).orElse(null);}

    public void deleteCategory(Long id) { expenseRepository.deleteById(id); }

}
