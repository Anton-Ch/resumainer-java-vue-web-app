package com.resumainer.service.pdf;

import com.resumainer.dao.ResumeBudgetConfigDao;
import com.resumainer.model.pdf.PagePlan;
import com.resumainer.service.ResumeBudgetConfigService;
import com.resumainer.service.WorkExperienceBudgetResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for PagePlanBuilder using production WorkExperienceBudgetResolver.
 */
@ExtendWith(MockitoExtension.class)
class PagePlanBuilderTest {

    @Mock
    private ResumeBudgetConfigService budgetConfigService;

    private WorkExperienceBudgetResolver resolver;
    private PagePlanBuilder builder;

    @BeforeEach
    void setUp() {
        resolver = new WorkExperienceBudgetResolver(budgetConfigService);
        builder = new PagePlanBuilder(resolver);
    }

    @Test
    void build_onePageScenario() {
        when(budgetConfigService.getWorkExperienceDistributionRules())
                .thenReturn(List.of(rule("one_page", 3, 0, null)));

        PagePlan plan = builder.build(2, 0, 1);
        assertEquals(1, plan.getTargetPageCount());
        // Budget resolver caps at available work items (2 < 3)
        assertEquals(2, plan.getPage1WorkCount());
        assertEquals(0, plan.getPage2AdditionalWorkCount());
    }

    @Test
    void build_twoPageWithProjects() {
        when(budgetConfigService.getWorkExperienceDistributionRules())
                .thenReturn(List.of(rule("two_page", 3, 3, 3)));

        PagePlan plan = builder.build(6, 3, 2);
        assertEquals(2, plan.getTargetPageCount());
        assertEquals(3, plan.getPage1WorkCount());
        assertEquals(3, plan.getPage2AdditionalWorkCount());
        assertEquals(3, plan.getPage2ProjectCount());
        assertTrue(plan.isPage2HasProjectsFirst());
    }

    @Test
    void build_twoPageNoProjects() {
        when(budgetConfigService.getWorkExperienceDistributionRules())
                .thenReturn(List.of(rule("two_page", 2, 2, 2)));

        PagePlan plan = builder.build(4, 0, 1);
        assertEquals(2, plan.getTargetPageCount());
        assertEquals(0, plan.getPage2ProjectCount());
        assertFalse(plan.isPage2HasProjectsFirst(), "No projects → projectsFirst=false");
    }

    @Test
    void build_singleWorkItem() {
        when(budgetConfigService.getWorkExperienceDistributionRules())
                .thenReturn(List.of(rule("one_page", 1, 0, null)));

        PagePlan plan = builder.build(1, 0, 0);
        assertEquals(1, plan.getTargetPageCount());
        assertEquals(1, plan.getPage1WorkCount());
    }

    private ResumeBudgetConfigDao.WorkExperienceDistributionRule rule(
            String templateMode, int page1Jobs, int page2Jobs, Integer page2MaxAdditional) {
        return new ResumeBudgetConfigDao.WorkExperienceDistributionRule(
                "test", 1, 10, 0, null, false,
                templateMode, page1Jobs, page2Jobs, page2MaxAdditional, 10);
    }
}
