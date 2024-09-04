package ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Category;


import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import ru.Tim.Proj.moneyAnalyzer.Models.Category.IncomeSource;
import ru.Tim.Proj.moneyAnalyzer.Repositoryes.Category.IncomeRepository;

import java.util.List;

@Service
public class IncomeSourceService {

    private final IncomeRepository incomeRepository;

    public IncomeSourceService(IncomeRepository incomeRepository) {
        this.incomeRepository = incomeRepository;
    }

    @PostConstruct
    public void checkStart(){
        Integer amount = checkFirstStart();
        if(amount.equals(0)){
            insertDefaultSources();
            System.out.println("Источники созданы успешно");
        }else {
            System.out.println(amount + ": Количество источников");
        }
    }

    public Integer checkFirstStart(){
        return incomeRepository.getSourceAmount();
    }

    private void insertDefaultSources() {
        List<IncomeSource> defaultSources = List.of(
                new IncomeSource("Зарплата", null),
                new IncomeSource("Пенсия", null),
                new IncomeSource("Стипендия", null),
                new IncomeSource("Пособия", null),
                new IncomeSource("Перевод", null)
        );

        for (IncomeSource source : defaultSources) {
            incomeRepository.save(source);
        }
    }

    public void createOrUpdate(IncomeSource incomeSource){
        incomeRepository.save(incomeSource);
    }

    public List<IncomeSource> getCategoryList(Long id){
        return incomeRepository.getSources(id);
    }

    public List<IncomeSource> getUserSourcesList(Long id) { return incomeRepository.getOnlyUserSources(id); }

    public IncomeSource findCategoryById(Long id) { return incomeRepository.findById(id).orElse(null);}

    public void deleteCategory(Long id) { incomeRepository.deleteById(id); }
}
