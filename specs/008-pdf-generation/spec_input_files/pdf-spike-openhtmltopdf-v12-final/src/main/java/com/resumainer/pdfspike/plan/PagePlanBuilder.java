package com.resumainer.pdfspike.plan;

import com.resumainer.pdfspike.model.*;

import java.util.List;

public final class PagePlanBuilder {
    public PagePlan build(ResumeData data, EdgeCaseRule rule, int targetPageCount) {
        List<WorkExperience> all = data.workExperience();
        int p1 = Math.min(rule.page1WorkItems(), all.size());
        int p2 = Math.min(rule.page2WorkItems(), Math.max(0, all.size() - p1));
        List<WorkExperience> page1 = all.subList(0, p1);
        List<WorkExperience> page2 = all.subList(p1, p1 + p2);
        return new PagePlan(rule, targetPageCount, page1, page2, data.projects(), true);
    }
}
