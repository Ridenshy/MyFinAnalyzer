package ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Plans;

import ru.Tim.Proj.moneyAnalyzer.Models.Plan.PlannedExpense;
import ru.Tim.Proj.moneyAnalyzer.Repositoryes.Plans.EPlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ePlannedService {

    private final EPlanRepository ePlanRepository;

    public ePlannedService(EPlanRepository ePlanRepository) {
        this.ePlanRepository = ePlanRepository;
    }

    public void addPlannedExpense(PlannedExpense plannedExpense){
        ePlanRepository.save(plannedExpense);
    }

    public void deletePlannedExpense(Long id){
        ePlanRepository.deleteById(id);
    }

    public List<PlannedExpense> getPlannedExpList(Long id, String yearMonth){
        return ePlanRepository.getCurrentMonthExpPlans(id, yearMonth);
    }

}
