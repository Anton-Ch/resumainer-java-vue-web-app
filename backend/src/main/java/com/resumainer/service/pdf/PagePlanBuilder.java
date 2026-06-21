package com.resumainer.service.pdf;

import com.resumainer.model.pdf.PagePlan;
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

    public PagePlanBuilder(WorkExperienceBudgetResolver budgetResolver) {
        this.budgetResolver = budgetResolver;
    }

    /**
     * Build a PagePlan from work experience, project, and course counts.
     * Uses production budget resolver for deterministic page allocation.
     */
    public PagePlan build(int totalWorkExperience, int totalProjects, int totalCourses) {
        WorkExperienceBudgetResolver.WorkExperienceBudget budget =
                budgetResolver.resolve(totalWorkExperience, totalCourses, totalProjects);

        PagePlan plan = new PagePlan();
        plan.setTargetPageCount("one_page".equals(budget.templateMode) ? 1 : 2);
        plan.setPage1WorkCount(budget.targetPage1Jobs);
        plan.setPage2AdditionalWorkCount(budget.targetPage2Jobs);
        plan.setPage2ProjectCount(totalProjects);
        plan.setPage2HasProjectsFirst(totalProjects > 0);

        log.debug("PagePlan: targetPages={}, p1Work={}, p2Work={}, p2Projects={}",
                plan.getTargetPageCount(), plan.getPage1WorkCount(),
                plan.getPage2AdditionalWorkCount(), plan.getPage2ProjectCount());
        return plan;
    }
}
