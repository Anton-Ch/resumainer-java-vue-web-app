package com.resumainer.service.pdf;

import com.resumainer.model.pdf.PagePlan;
import com.resumainer.model.pdf.ResumeRenderData;
import com.resumainer.service.WorkExperienceBudgetResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Assembles ResumeRenderData from production profile and generation response data.
 * Also builds PagePlan using the existing WorkExperienceBudgetResolver.
 *
 * The caller (ResumeFinalizeService) is responsible for loading the raw data
 * from DAOs and passing it to this builder. This keeps the builder focused on
 * data transformation, not data loading.
 */
@Service
public class ResumeRenderDataBuilder {

    private static final Logger log = LoggerFactory.getLogger(ResumeRenderDataBuilder.class);

    private final WorkExperienceBudgetResolver budgetResolver;

    public ResumeRenderDataBuilder(WorkExperienceBudgetResolver budgetResolver) {
        this.budgetResolver = budgetResolver;
    }

    /**
     * Build a PagePlan from work experience counts and budget rules.
     * Uses production WorkExperienceBudgetResolver (NOT spike edge_case_rule — FR-008-024).
     */
    public PagePlan buildPagePlan(int totalWorkExperience, int totalProjects, int totalCourses) {
        WorkExperienceBudgetResolver.WorkExperienceBudget budget =
                budgetResolver.resolve(totalWorkExperience, totalCourses, totalProjects);

        PagePlan plan = new PagePlan();
        plan.setTargetPageCount("one_page".equals(budget.templateMode) ? 1 : 2);
        plan.setPage1WorkCount(budget.targetPage1Jobs);
        plan.setPage2AdditionalWorkCount(budget.targetPage2Jobs);
        plan.setPage2ProjectCount(Math.min(totalProjects, budget.targetPage1Jobs > 0 ? totalProjects : 0));
        plan.setPage2HasProjectsFirst(totalProjects > 0);

        log.debug("PagePlan: targetPages={}, p1Work={}, p2Work={}, p2Projects={}",
                plan.getTargetPageCount(), plan.getPage1WorkCount(),
                plan.getPage2AdditionalWorkCount(), plan.getPage2ProjectCount());
        return plan;
    }

    /**
     * Assemble a populated ResumeRenderData from the provided inputs.
     * Caller is responsible for loading data and passing it in.
     */
    public ResumeRenderData buildRenderData(RenderDataInput input) {
        ResumeRenderData data = new ResumeRenderData();
        data.setLanguageCode(input.languageCode);
        data.setFullName(input.fullName);
        data.setProfessionalTitle(input.professionalTitle);
        data.setPhone(input.phone);
        data.setEmail(input.email);
        data.setLocation(input.location);
        data.setLinkedin(input.linkedin);
        data.setPortfolio(input.portfolio);
        data.setTelegram(input.telegram);
        data.setWhatsapp(input.whatsapp);
        data.setValueLine(input.valueLine);
        data.setProfessionalSummary(input.professionalSummary);
        data.setProfessionalAspirations(input.professionalAspirations);
        data.setCoverLetter(input.coverLetter);

        // Personal info lines — omit when blank (FR-008-023 optional handling)
        if (input.personalLine1 != null && !input.personalLine1.isBlank()) {
            data.setPersonalLine1(input.personalLine1);
        }
        if (input.personalLine2 != null && !input.personalLine2.isBlank()) {
            data.setPersonalLine2(input.personalLine2);
        }
        if (input.personalLine3 != null && !input.personalLine3.isBlank()) {
            data.setPersonalLine3(input.personalLine3);
        }

        // Work experience with edited bullets
        if (input.workItems != null) {
            data.setWorkExperience(input.workItems);
        }
        // Projects with edited bullets
        if (input.projectItems != null) {
            data.setProjects(input.projectItems);
        }
        // Courses
        if (input.courseItems != null) {
            data.setCourses(input.courseItems);
        }
        // Skills
        if (input.skillGroups != null) {
            data.setSkills(input.skillGroups);
        }
        // Education (profile-owned, bilingual)
        if (input.educationLines != null) {
            data.setEducation(input.educationLines);
        }

        return data;
    }

    /**
     * Flat input DTO for ResumeRenderDataBuilder.
     * Caller populates this from profile + generation response DAOs.
     */
    public static class RenderDataInput {
        public String languageCode;
        public String fullName;
        public String professionalTitle;
        public String phone;
        public String email;
        public String location;
        public String linkedin;
        public String portfolio;
        public String telegram;
        public String whatsapp;
        public String valueLine;
        public String professionalSummary;
        public String professionalAspirations;
        public String coverLetter;
        public String personalLine1;
        public String personalLine2;
        public String personalLine3;

        public List<ResumeRenderData.RenderWorkItem> workItems = new ArrayList<>();
        public List<ResumeRenderData.RenderProjectItem> projectItems = new ArrayList<>();
        public List<ResumeRenderData.RenderCourseItem> courseItems = new ArrayList<>();
        public List<ResumeRenderData.RenderSkillGroup> skillGroups = new ArrayList<>();
        public List<String> educationLines = new ArrayList<>();
    }
}
