package ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Plans;

import ru.Tim.Proj.moneyAnalyzer.Models.Plan.PlannedExpense;
import ru.Tim.Proj.moneyAnalyzer.Repositoryes.Plans.EPlanRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class EPlannedService {

    private final EPlanRepository ePlanRepository;

    public EPlannedService(EPlanRepository ePlanRepository) {
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

    public BigDecimal getTotalPlanAmount(Long id, String date){return ePlanRepository.getTotalPlanSum(id, date);}

}
