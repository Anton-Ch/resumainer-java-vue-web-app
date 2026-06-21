package com.resumainer.service.pdf;

import com.resumainer.dao.ResumeBudgetConfigDao;
import com.resumainer.model.pdf.PagePlan;
import com.resumainer.model.pdf.ResumeRenderData;
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
 * Tests for ResumeRenderDataBuilder: PagePlan generation and render data assembly.
 */
@ExtendWith(MockitoExtension.class)
class ResumeRenderDataBuilderTest {

    @Mock
    private ResumeBudgetConfigService budgetConfigService;

    private WorkExperienceBudgetResolver budgetResolver;
    private ResumeRenderDataBuilder builder;

    @BeforeEach
    void setUp() {
        budgetResolver = new WorkExperienceBudgetResolver(budgetConfigService);
        builder = new ResumeRenderDataBuilder(budgetResolver);
    }

    @Test
    void buildPagePlan_onePageScenario() {
        when(budgetConfigService.getWorkExperienceDistributionRules())
                .thenReturn(List.of(rule("one_page", 3, 0, 0)));

        PagePlan plan = builder.buildPagePlan(2, 0, 1);
        assertNotNull(plan);
        assertEquals(1, plan.getTargetPageCount());
    }

    @Test
    void buildPagePlan_withProjects_twoPage() {
        when(budgetConfigService.getWorkExperienceDistributionRules())
                .thenReturn(List.of(rule("two_page", 3, 2, 2)));

        PagePlan plan = builder.buildPagePlan(4, 2, 1);
        assertNotNull(plan);
        assertEquals(2, plan.getTargetPageCount());
    }

    private ResumeBudgetConfigDao.WorkExperienceDistributionRule rule(
            String templateMode, int page1Jobs, int page2Jobs, Integer page2MaxAdditional) {
        return new ResumeBudgetConfigDao.WorkExperienceDistributionRule(
                "test-rule", 1, 10, 0, null, false,
                templateMode, page1Jobs, page2Jobs, page2MaxAdditional, 10);
    }

    @Test
    void buildRenderData_assemblesAllFields() {
        ResumeRenderDataBuilder.RenderDataInput input = new ResumeRenderDataBuilder.RenderDataInput();
        input.languageCode = "EN";
        input.fullName = "John Doe";
        input.professionalTitle = "Engineer";
        input.email = "john@test.com";
        input.professionalSummary = "Experienced developer";
        input.professionalAspirations = "Lead teams";

        ResumeRenderData data = builder.buildRenderData(input);

        assertEquals("EN", data.getLanguageCode());
        assertEquals("John Doe", data.getFullName());
        assertEquals("Engineer", data.getProfessionalTitle());
        assertEquals("john@test.com", data.getEmail());
        assertEquals("Experienced developer", data.getProfessionalSummary());
    }

    @Test
    void buildRenderData_omitsBlankPersonalLines() {
        ResumeRenderDataBuilder.RenderDataInput input = new ResumeRenderDataBuilder.RenderDataInput();
        input.languageCode = "EN";
        input.personalLine1 = "Location: NY";
        input.personalLine2 = "";    // blank → omitted
        input.personalLine3 = null;  // null → omitted

        ResumeRenderData data = builder.buildRenderData(input);

        assertEquals("Location: NY", data.getPersonalLine1());
        assertNull(data.getPersonalLine2());
        assertNull(data.getPersonalLine3());
    }

    @Test
    void buildRenderData_passesThroughWorkItemsWithBullets() {
        ResumeRenderDataBuilder.RenderDataInput input = new ResumeRenderDataBuilder.RenderDataInput();
        input.languageCode = "EN";
        ResumeRenderData.RenderWorkItem work = new ResumeRenderData.RenderWorkItem();
        work.setJobTitle("Developer");
        work.setBulletPoints(List.of("Built API", "Reduced latency"));
        input.workItems.add(work);

        ResumeRenderData data = builder.buildRenderData(input);

        assertEquals(1, data.getWorkExperience().size());
        assertEquals(2, data.getWorkExperience().get(0).getBulletPoints().size());
    }
}
