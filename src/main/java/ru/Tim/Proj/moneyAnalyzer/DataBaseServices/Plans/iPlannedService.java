package ru.Tim.Proj.moneyAnalyzer.DataBaseServices.Plans;

import ru.Tim.Proj.moneyAnalyzer.Models.Plan.PlannedIncome;
import ru.Tim.Proj.moneyAnalyzer.Repositoryes.Plans.iPlanRepository;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.List;

@Service
public class iPlannedService {

    private final iPlanRepository iPlanRepository;

    public iPlannedService(ru.Tim.Proj.moneyAnalyzer.Repositoryes.Plans.iPlanRepository iPlanRepository) {
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
