package com.resumainer.pdfspike.plan;

import com.resumainer.pdfspike.model.*;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class PagePlanBuilderTest {
    @Test void putsProjectsBeforeAdditionalWorkOnPageTwoFlag() {
        ResumeData data = sample(6,3);
        EdgeCaseRule rule = new EdgeCaseRule(15,6,6,3,5,TemplateMode.TWO_PAGE,3,3,6,"six jobs projects");
        PagePlan plan = new PagePlanBuilder().build(data, rule, 2);
        assertEquals(3, plan.page1Work().size());
        assertEquals(3, plan.page2AdditionalWork().size());
        assertEquals(3, plan.page2Projects().size());
        assertTrue(plan.page2HasProjectsFirst());
    }

    @Test void onePageHasNoAdditionalWork() {
        ResumeData data = sample(1,0);
        EdgeCaseRule rule = new EdgeCaseRule(1,1,1,0,1,TemplateMode.ONE_PAGE,1,0,1,"one job");
        PagePlan plan = new PagePlanBuilder().build(data, rule, 1);
        assertEquals(1, plan.page1Work().size());
        assertTrue(plan.page2AdditionalWork().isEmpty());
        assertTrue(plan.page2Projects().isEmpty());
    }

    private ResumeData sample(int work, int projects) {
        List<WorkExperience> we = new ArrayList<>();
        for (int i = 0; i < work; i++) we.add(new WorkExperience("r"+i,"c","l","s","e","d",List.of("b")));
        List<ProjectItem> ps = new ArrayList<>();
        for (int i = 0; i < projects; i++) ps.add(new ProjectItem("p"+i,"r","l","s","e","d",List.of("b")));
        return new ResumeData(Language.EN,1,"n","t","p","e","l","li","po","tg","wa","v","sum",we,List.of(),List.of(),List.of(),ps,"asp","p1","p2","p3");
    }
}
