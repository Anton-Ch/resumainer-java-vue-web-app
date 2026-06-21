package com.resumainer.service.pdf;

import com.resumainer.model.pdf.ResumeRenderData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ResumeRenderDataBuilder — render data assembly only.
 * Page plan logic moved to PagePlanBuilder.
 */
class ResumeRenderDataBuilderTest {

    private ResumeRenderDataBuilder builder;

    @BeforeEach
    void setUp() {
        builder = new ResumeRenderDataBuilder();
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
    }

    @Test
    void buildRenderData_omitsBlankPersonalLines() {
        ResumeRenderDataBuilder.RenderDataInput input = new ResumeRenderDataBuilder.RenderDataInput();
        input.languageCode = "EN";
        input.personalLine1 = "Location: NY";
        input.personalLine2 = "";
        input.personalLine3 = null;

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
