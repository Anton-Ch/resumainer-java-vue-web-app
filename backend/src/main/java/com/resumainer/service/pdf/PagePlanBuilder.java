package com.resumainer.service.pdf;

import com.resumainer.model.pdf.PagePlan;
import com.resumainer.service.ResumeBudgetConfigService;
import com.resumainer.service.WorkExperienceBudgetResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Builds a PagePlan from production budget configuration.
 * Uses WorkExperienceBudgetResolver (NOT spike edge_case_rule — FR-008-024).
 */
@Service
public class PagePlanBuilder {

    private static final Logger log = LoggerFactory.getLogger(PagePlanBuilder.class);

    private final WorkExperienceBudgetResolver budgetResolver;
    private final ResumeBudgetConfigService budgetConfigService;

    public PagePlanBuilder(WorkExperienceBudgetResolver budgetResolver,
                           ResumeBudgetConfigService budgetConfigService) {
        this.budgetResolver = budgetResolver;
        this.budgetConfigService = budgetConfigService;
    }

    /**
     * Build a PagePlan from work experience, project, and course counts.
     * Uses production budget resolver for deterministic page allocation.
     * If page 2 has zero items, downgrades to a single-page plan.
     */
    public PagePlan build(int totalWorkExperience, int totalProjects, int totalCourses) {
        WorkExperienceBudgetResolver.WorkExperienceBudget budget =
                budgetResolver.resolve(totalWorkExperience, totalCourses, totalProjects);

        int page2ProjectCount = resolvePage2ProjectCount(totalProjects);

        PagePlan plan = new PagePlan();
        boolean isOnePage = "one_page".equals(budget.templateMode);
        plan.setPage1WorkCount(budget.targetPage1Jobs);
        plan.setPage2AdditionalWorkCount(budget.targetPage2Jobs);
        plan.setPage2ProjectCount(page2ProjectCount);
        plan.setPage2HasProjectsFirst(page2ProjectCount > 0);

        boolean page2IsEmpty = budget.targetPage2Jobs == 0 && page2ProjectCount == 0;
        plan.setTargetPageCount(isOnePage || page2IsEmpty ? 1 : 2);

        log.debug("PagePlan: targetPages={}, p1Work={}, p2Work={}, p2Projects={}, sourceProjects={}",
                plan.getTargetPageCount(), plan.getPage1WorkCount(),
                plan.getPage2AdditionalWorkCount(), plan.getPage2ProjectCount(), totalProjects);
        return plan;
    }

    private int resolvePage2ProjectCount(int totalProjects) {
        if (totalProjects <= 0) return 0;
        int maxProjects;
        try {
            maxProjects = budgetConfigService.getMaxProjects();
        } catch (RuntimeException e) {
            log.warn("Failed to load max project budget; falling back to all source projects: {}", e.getMessage());
            maxProjects = totalProjects;
        }
        if (maxProjects <= 0) return 0;
        return Math.min(totalProjects, maxProjects);
    }
}
