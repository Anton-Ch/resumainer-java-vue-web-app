package com.resumainer.service.pdf;

import com.resumainer.model.pdf.PagePlan;
import com.resumainer.model.pdf.ResumeRenderData;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ContentExpectationBuilderTest {

    private final ContentExpectationBuilder builder = new ContentExpectationBuilder();

    @Test
    void build_returnsExpectedAnchors() {
        ResumeRenderData data = new ResumeRenderData();
        data.setLanguageCode("EN");
        data.setFullName("John Doe");
        data.setProfessionalTitle("Engineer");
        data.setEmail("john@test.com");
        data.setValueLine("Building software");
        data.setProfessionalAspirations("Grow as a leader");
        data.setPersonalLine1("Location: NY");
        data.getSkills().add(newSkillGroup("Java", List.of("Spring")));

        PagePlan plan = new PagePlan();
        plan.setTargetPageCount(1);

        List<String> expected = builder.build(data, plan);

        assertFalse(expected.isEmpty());
        assertTrue(expected.contains("John Doe"));
        assertTrue(expected.contains("Engineer"));
        assertTrue(expected.contains("john@test.com"));
    }

    @Test
    void build_emptyData_returnsOnlySectionTitles() {
        ResumeRenderData data = new ResumeRenderData();
        data.setLanguageCode("EN");
        PagePlan plan = new PagePlan();
        plan.setTargetPageCount(1);
        List<String> expected = builder.build(data, plan);
        // Section titles are always added because their strings are non-blank
        // But other anchors (name, title, email) are skipped when empty
        assertFalse(expected.contains("null"), "Should not contain null strings");
        assertFalse(expected.isEmpty(), "Section titles should be present even with empty data");
    }

    private static ResumeRenderData.RenderSkillGroup newSkillGroup(String name, List<String> skills) {
        ResumeRenderData.RenderSkillGroup g = new ResumeRenderData.RenderSkillGroup();
        g.setGroupName(name);
        g.setSkills(skills);
        return g;
    }
}
