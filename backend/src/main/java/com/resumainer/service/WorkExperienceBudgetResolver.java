package com.resumainer.service;

import com.resumainer.dao.ResumeBudgetConfigDao.WorkExperienceDistributionRule;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

/**
 * Resolves Work Experience section budget from DB-backed distribution rules.
 *
 * The resolver selects exactly one resume_work_experience_distribution_rules row
 * by profile counts and priority. It does not decide which jobs are more relevant;
 * it only calculates how many workExperience records may be returned for Page 1
 * and Page 2. Prompt rules and the AI model use the resulting counts to select
 * current / relevant / recent jobs within the resolved limit.
 */
@Service
public class WorkExperienceBudgetResolver {

    private final ResumeBudgetConfigService budgetConfigService;

    public WorkExperienceBudgetResolver(ResumeBudgetConfigService budgetConfigService) {
        this.budgetConfigService = budgetConfigService;
    }

    public WorkExperienceBudget resolve(int totalJobs, int totalCourses, int totalProjects) {
        if (totalJobs <= 0) {
            throw new IllegalArgumentException("totalJobs must be positive");
        }
        if (totalCourses < 0) {
            throw new IllegalArgumentException("totalCourses must not be negative");
        }
        if (totalProjects < 0) {
            throw new IllegalArgumentException("totalProjects must not be negative");
        }

        List<WorkExperienceDistributionRule> rules = budgetConfigService.getWorkExperienceDistributionRules();
        if (rules == null || rules.isEmpty()) {
            throw new IllegalStateException("No work experience distribution rules configured");
        }

        WorkExperienceDistributionRule rule = rules.stream()
                .sorted(Comparator
                        .comparingInt((WorkExperienceDistributionRule r) -> r.priority)
                        .thenComparing(r -> r.caseKey))
                .filter(r -> matches(r, totalJobs, totalCourses, totalProjects))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No work experience distribution rule matches profile counts: "
                                + "totalJobs=" + totalJobs
                                + ", totalCourses=" + totalCourses
                                + ", totalProjects=" + totalProjects));

        int page1Capacity = Math.max(rule.page1Jobs, 0);
        int page2Capacity = resolvePage2Capacity(rule);

        int targetPage1Jobs = Math.min(page1Capacity, totalJobs);
        int remainingAfterPage1 = Math.max(totalJobs - targetPage1Jobs, 0);
        int targetPage2Jobs = Math.min(page2Capacity, remainingAfterPage1);
        int maxTotalJobs = targetPage1Jobs + targetPage2Jobs;

        return new WorkExperienceBudget(
                rule.caseKey,
                rule.templateMode,
                totalJobs,
                totalCourses,
                totalProjects,
                page1Capacity,
                page2Capacity,
                targetPage1Jobs,
                targetPage2Jobs,
                maxTotalJobs
        );
    }

    private boolean matches(
            WorkExperienceDistributionRule rule,
            int totalJobs,
            int totalCourses,
            int totalProjects
    ) {
        if (totalJobs < rule.minTotalJobs || totalJobs > rule.maxTotalJobs) {
            return false;
        }
        if (totalProjects < rule.minProjects) {
            return false;
        }
        if (rule.maxProjects != null && totalProjects > rule.maxProjects) {
            return false;
        }
        return !rule.requireNoCourses || totalCourses == 0;
    }

    private int resolvePage2Capacity(WorkExperienceDistributionRule rule) {
        int page2Jobs = Math.max(rule.page2Jobs, 0);
        if (rule.page2MaxAdditionalJobs == null) {
            return page2Jobs;
        }
        return Math.min(page2Jobs, Math.max(rule.page2MaxAdditionalJobs, 0));
    }

    public static class WorkExperienceBudget {
        public final String caseKey;
        public final String templateMode;

        public final int totalProfileJobs;
        public final int totalProfileCourses;
        public final int totalProfileProjects;

        public final int page1Capacity;
        public final int page2Capacity;

        public final int targetPage1Jobs;
        public final int targetPage2Jobs;
        public final int maxTotalJobs;

        public WorkExperienceBudget(
                String caseKey,
                String templateMode,
                int totalProfileJobs,
                int totalProfileCourses,
                int totalProfileProjects,
                int page1Capacity,
                int page2Capacity,
                int targetPage1Jobs,
                int targetPage2Jobs,
                int maxTotalJobs
        ) {
            this.caseKey = caseKey;
            this.templateMode = templateMode;
            this.totalProfileJobs = totalProfileJobs;
            this.totalProfileCourses = totalProfileCourses;
            this.totalProfileProjects = totalProfileProjects;
            this.page1Capacity = page1Capacity;
            this.page2Capacity = page2Capacity;
            this.targetPage1Jobs = targetPage1Jobs;
            this.targetPage2Jobs = targetPage2Jobs;
            this.maxTotalJobs = maxTotalJobs;
        }
    }
}
