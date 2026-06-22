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
    void build_emptyData_doesNotContainNullOrBlankAnchors() {
        ResumeRenderData data = new ResumeRenderData();
        data.setLanguageCode("EN");
        PagePlan plan = new PagePlan();
        plan.setTargetPageCount(1);

        List<String> expected = builder.build(data, plan);

        assertFalse(expected.contains("null"), "Should not contain null strings");
        assertTrue(expected.stream().noneMatch(String::isBlank), "Should not contain blank anchors");
    }

    @Test
    void build_includesStableEducationAndBulletAnchorsWithoutLongDescriptions() {
        ResumeRenderData data = new ResumeRenderData();
        data.setLanguageCode("EN");
        data.setFullName("Vasya Pupkin");
        data.setProfessionalTitle("Business Analyst");
        data.setEmail("vasya@example.com");
        data.setValueLine("BA | SQL | BPMN");
        data.setProfessionalSummary("Experienced analyst with delivery background.");
        data.setEducation(List.of("Bachelor: Information Systems | KAFU"));

        ResumeRenderData.RenderWorkItem work = new ResumeRenderData.RenderWorkItem();
        work.setJobTitle("Business Analyst");
        work.setCompanyName("Bobrosoft");
        work.setDescription("Gathered requirements and supported dashboard delivery with multiple stakeholders and long details.");
        work.setBulletPoints(List.of("Defined acceptance criteria for dashboard delivery, stakeholder review, and QA handoff."));
        data.setWorkExperience(List.of(work));

        ResumeRenderData.RenderProjectItem project = new ResumeRenderData.RenderProjectItem();
        project.setProjectName("AI Resume Generation Platform");
        project.setRole("Product Owner / Backend Developer");
        project.setDescription("Built an AI-assisted resume flow with long implementation notes.");
        project.setBulletPoints(List.of("Implemented structured generation and review flow for semantic PDF validation."));
        data.setProjects(List.of(project));

        PagePlan plan = new PagePlan();
        plan.setTargetPageCount(2);
        plan.setPage1WorkCount(1);
        plan.setPage2AdditionalWorkCount(0);
        plan.setPage2ProjectCount(1);

        List<String> expected = builder.build(data, plan);

        assertTrue(expected.contains("BACHELOR INFORMATION SYSTEMS KAFU"));
        assertTrue(expected.contains("DEFINED ACCEPTANCE CRITERIA FOR DASHBOARD DELIVERY STAKEHOLDER REVIEW"));
        assertTrue(expected.contains("IMPLEMENTED STRUCTURED GENERATION AND REVIEW FLOW FOR SEMANTIC"));

        assertFalse(expected.contains(work.getDescription()), "Long work descriptions must not be required verbatim");
        assertFalse(expected.contains(project.getDescription()), "Long project descriptions must not be required verbatim");
    }

    @Test
    void buildForPlannedPage_page1UsesOnlyPage1StableAnchors() {
        ResumeRenderData data = new ResumeRenderData();
        data.setLanguageCode("EN");
        data.setFullName("Vasya Pupkin");
        data.setProfessionalTitle("Business Analyst");
        data.setEmail("vasya@example.com");
        data.setValueLine("BA | SQL | BPMN");
        data.setProfessionalSummary("Summary text.");
        data.setEducation(List.of("Bachelor: Information Systems | KAFU"));

        ResumeRenderData.RenderWorkItem page1Work = new ResumeRenderData.RenderWorkItem();
        page1Work.setJobTitle("Business Analyst");
        page1Work.setCompanyName("Bobrosoft");
        page1Work.setBulletPoints(List.of("Defined acceptance criteria for dashboard delivery."));

        ResumeRenderData.RenderWorkItem page2Work = new ResumeRenderData.RenderWorkItem();
        page2Work.setJobTitle("Old Role");
        page2Work.setCompanyName("Old Company");
        page2Work.setBulletPoints(List.of("Maintained legacy process documentation."));

        data.setWorkExperience(List.of(page1Work, page2Work));

        PagePlan plan = new PagePlan();
        plan.setTargetPageCount(2);
        plan.setPage1WorkCount(1);
        plan.setPage2AdditionalWorkCount(1);
        plan.setPage2ProjectCount(0);

        List<String> expected = builder.buildForPlannedPage(data, plan, 1);

        assertTrue(expected.contains("Business Analyst"));
        assertTrue(expected.contains("Bobrosoft"));
        assertTrue(expected.contains("DEFINED ACCEPTANCE CRITERIA FOR DASHBOARD DELIVERY"));
        assertFalse(expected.contains("Old Role"));
        assertFalse(expected.contains("MAINTAINED LEGACY PROCESS DOCUMENTATION"));
    }

    private static ResumeRenderData.RenderSkillGroup newSkillGroup(String name, List<String> skills) {
        ResumeRenderData.RenderSkillGroup g = new ResumeRenderData.RenderSkillGroup();
        g.setGroupName(name);
        g.setSkills(skills);
        return g;
    }
}
