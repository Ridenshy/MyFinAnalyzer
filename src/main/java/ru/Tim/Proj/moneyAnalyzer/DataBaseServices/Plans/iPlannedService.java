package ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Plans;

import ru.Tim.Proj.moneyAnalyzer.Models.Plan.PlannedIncome;
import ru.Tim.Proj.moneyAnalyzer.Repositoryes.Plans.IPlanRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class iPlannedService {

    private final IPlanRepository iPlanRepository;

    public iPlannedService(IPlanRepository iPlanRepository) {
        this.iPlanRepository = iPlanRepository;
    }

    public void addIncomePlan(PlannedIncome plannedIncome){
        iPlanRepository.save(plannedIncome);
    }

    public void deleteIncomePlan(Long id){
        iPlanRepository.deleteById(id);
    }

    public List<PlannedIncome> getPlannedIncList(Long id, String yearMonth){
        return iPlanRepository.getCurrentMonthIncPlans(id, yearMonth);
    }
}
