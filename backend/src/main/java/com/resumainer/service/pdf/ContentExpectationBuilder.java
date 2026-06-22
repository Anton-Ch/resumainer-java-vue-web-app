package com.resumainer.service.pdf;

import com.resumainer.model.pdf.PagePlan;
import com.resumainer.model.pdf.ResumeRenderData;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds expected content strings for PDF validation.
 *
 * Important:
 * This builder must NOT require long AI-generated paragraphs verbatim.
 * PDF text extraction can change punctuation, line breaks, separators, bullets,
 * and hyphenation. Therefore semantic validation uses short stable token anchors:
 * names, titles, section labels, job/company/project names, education lines,
 * and one representative bullet anchor per rendered section.
 */
public final class ContentExpectationBuilder {

    private static final int EDUCATION_ANCHOR_TOKENS = 10;
    private static final int BULLET_ANCHOR_TOKENS = 8;
    private static final int PERSONAL_ANCHOR_TOKENS = 12;

    /** Build expected text anchors for the full resume across all pages. */
    public List<String> build(ResumeRenderData data, PagePlan plan) {
        List<String> expected = new ArrayList<>();

        addHeaderExpected(expected, data);

        // Page 1 renderer always emits the summary section title.
        addIfPresent(expected, sectionTitle(data, "Professional Summary", "О себе"));

        List<ResumeRenderData.RenderWorkItem> p1Work = page1Work(data, plan);
        if (!p1Work.isEmpty()) {
            addIfPresent(expected, sectionTitle(data, "Work Experience", "Опыт работы"));
            addWorkExpected(expected, p1Work);
        }

        if (data.getSkills() != null && !data.getSkills().isEmpty()) {
            addIfPresent(expected, sectionTitle(data, "Skills", "Навыки"));
            for (ResumeRenderData.RenderSkillGroup group : data.getSkills()) {
                addIfPresent(expected, group.getGroupName());
            }
        }

        if (data.getEducation() != null && !data.getEducation().isEmpty()) {
            addIfPresent(expected, sectionTitle(data, "Education", "Образование"));
            for (String line : data.getEducation()) {
                addStableAnchor(expected, line, EDUCATION_ANCHOR_TOKENS);
            }
        }

        if (data.getCourses() != null && !data.getCourses().isEmpty()) {
            addIfPresent(expected, sectionTitle(data, "Courses and Certifications", "Курсы и сертификаты"));
            for (ResumeRenderData.RenderCourseItem c : data.getCourses()) {
                addIfPresent(expected, c.getName());
                addIfPresent(expected, c.getProvider());
            }
        }

        List<ResumeRenderData.RenderProjectItem> projects = page2Projects(data, plan);
        if (!projects.isEmpty()) {
            addIfPresent(expected, sectionTitle(data, "Projects and Volunteering", "Проекты и волонтёрство"));
            addProjectExpected(expected, projects);
        }

        List<ResumeRenderData.RenderWorkItem> p2Work = page2Work(data, plan);
        if (!p2Work.isEmpty()) {
            addIfPresent(expected, sectionTitle(data, "Additional Work Experience", "Дополнительный опыт работы"));
            addWorkExpected(expected, p2Work);
        }

        addAspirationsExpected(expected, data);
        addPersonalExpected(expected, data);

        return uniqueNonBlank(expected);
    }

    /** Build expected text anchors for a single planned page during isolated fitting. */
    public List<String> buildForPlannedPage(ResumeRenderData data, PagePlan plan, int plannedPageNumber) {
        List<String> expected = new ArrayList<>();

        if (plannedPageNumber == 1) {
            addHeaderExpected(expected, data);
            addIfPresent(expected, sectionTitle(data, "Professional Summary", "О себе"));

            List<ResumeRenderData.RenderWorkItem> p1Work = page1Work(data, plan);
            if (!p1Work.isEmpty()) {
                addIfPresent(expected, sectionTitle(data, "Work Experience", "Опыт работы"));
                addWorkExpected(expected, p1Work);
            }

            if (data.getSkills() != null && !data.getSkills().isEmpty()) {
                addIfPresent(expected, sectionTitle(data, "Skills", "Навыки"));
                for (ResumeRenderData.RenderSkillGroup group : data.getSkills()) {
                    addIfPresent(expected, group.getGroupName());
                }
            }

            if (data.getEducation() != null && !data.getEducation().isEmpty()) {
                addIfPresent(expected, sectionTitle(data, "Education", "Образование"));
                for (String line : data.getEducation()) {
                    addStableAnchor(expected, line, EDUCATION_ANCHOR_TOKENS);
                }
            }

            if (data.getCourses() != null && !data.getCourses().isEmpty()) {
                addIfPresent(expected, sectionTitle(data, "Courses and Certifications", "Курсы и сертификаты"));
                for (ResumeRenderData.RenderCourseItem c : data.getCourses()) {
                    addIfPresent(expected, c.getName());
                    addIfPresent(expected, c.getProvider());
                }
            }

            if (plan.getTargetPageCount() == 1) {
                addAspirationsExpected(expected, data);
                addPersonalExpected(expected, data);
            }
        } else if (plannedPageNumber == 2) {
            List<ResumeRenderData.RenderProjectItem> projects = page2Projects(data, plan);
            if (!projects.isEmpty()) {
                addIfPresent(expected, sectionTitle(data, "Projects and Volunteering", "Проекты и волонтёрство"));
                addProjectExpected(expected, projects);
            }

            List<ResumeRenderData.RenderWorkItem> p2Work = page2Work(data, plan);
            if (!p2Work.isEmpty()) {
                addIfPresent(expected, sectionTitle(data, "Additional Work Experience", "Дополнительный опыт работы"));
                addWorkExpected(expected, p2Work);
            }

            if (plan.getTargetPageCount() <= 2) {
                addAspirationsExpected(expected, data);
                addPersonalExpected(expected, data);
            }
        } else if (plannedPageNumber == 3) {
            addAspirationsExpected(expected, data);
            addPersonalExpected(expected, data);
        }

        return uniqueNonBlank(expected);
    }

    private void addWorkExpected(List<String> expected, List<ResumeRenderData.RenderWorkItem> items) {
        for (ResumeRenderData.RenderWorkItem w : items) {
            addIfPresent(expected, w.getJobTitle());
            addIfPresent(expected, w.getCompanyName());
        }
        addFirstAvailableWorkBulletAnchor(expected, items);
    }

    private void addProjectExpected(List<String> expected, List<ResumeRenderData.RenderProjectItem> projects) {
        for (ResumeRenderData.RenderProjectItem p : projects) {
            addIfPresent(expected, p.getProjectName());
            addIfPresent(expected, p.getRole());
        }
        addFirstAvailableProjectBulletAnchor(expected, projects);
    }

    private void addFirstAvailableWorkBulletAnchor(List<String> expected, List<ResumeRenderData.RenderWorkItem> items) {
        if (items == null || items.isEmpty()) return;

        for (ResumeRenderData.RenderWorkItem item : items) {
            if (item == null || item.getBulletPoints() == null) continue;
            if (addFirstBulletAnchor(expected, item.getBulletPoints())) return;
        }
    }

    private void addFirstAvailableProjectBulletAnchor(List<String> expected, List<ResumeRenderData.RenderProjectItem> projects) {
        if (projects == null || projects.isEmpty()) return;

        for (ResumeRenderData.RenderProjectItem project : projects) {
            if (project == null || project.getBulletPoints() == null) continue;
            if (addFirstBulletAnchor(expected, project.getBulletPoints())) return;
        }
    }

    private boolean addFirstBulletAnchor(List<String> expected, List<String> bullets) {
        for (String bullet : bullets) {
            if (hasText(bullet)) {
                addStableAnchor(expected, bullet, BULLET_ANCHOR_TOKENS);
                return true;
            }
        }
        return false;
    }

    private void addStableAnchor(List<String> expected, String text, int maxTokens) {
        if (!hasText(text)) return;

        List<String> tokens = anchorTokens(text);
        if (tokens.isEmpty()) return;

        int count = Math.min(Math.max(1, maxTokens), tokens.size());
        addIfPresent(expected, String.join(" ", tokens.subList(0, count)));
    }

    private List<String> anchorTokens(String text) {
        String normalized = PdfValidationService.normalize(text);
        if (!hasText(normalized)) return List.of();
        return List.of(normalized.split(" "));
    }

    private List<ResumeRenderData.RenderWorkItem> page1Work(ResumeRenderData data, PagePlan plan) {
        List<ResumeRenderData.RenderWorkItem> all = data.getWorkExperience();
        if (all == null || all.isEmpty()) return List.of();
        int count = Math.min(plan.getPage1WorkCount(), all.size());
        return all.subList(0, count);
    }

    private List<ResumeRenderData.RenderWorkItem> page2Work(ResumeRenderData data, PagePlan plan) {
        List<ResumeRenderData.RenderWorkItem> all = data.getWorkExperience();
        if (all == null || all.isEmpty()) return List.of();
        int p1Count = Math.min(plan.getPage1WorkCount(), all.size());
        int p2Count = Math.min(plan.getPage2AdditionalWorkCount(), all.size() - p1Count);
        if (p2Count <= 0) return List.of();
        return all.subList(p1Count, p1Count + p2Count);
    }

    private List<ResumeRenderData.RenderProjectItem> page2Projects(ResumeRenderData data, PagePlan plan) {
        List<ResumeRenderData.RenderProjectItem> all = data.getProjects();
        if (all == null || all.isEmpty()) return List.of();
        int count = Math.min(plan.getPage2ProjectCount(), all.size());
        if (count <= 0) return List.of();
        return all.subList(0, count);
    }

    private void addHeaderExpected(List<String> expected, ResumeRenderData data) {
        addIfPresent(expected, data.getFullName());
        addIfPresent(expected, data.getProfessionalTitle());
        addIfPresent(expected, data.getEmail());
        addIfPresent(expected, data.getValueLine());
    }

    private void addAspirationsExpected(List<String> expected, ResumeRenderData data) {
        if (hasText(data.getProfessionalAspirations())) {
            addIfPresent(expected, sectionTitle(data, "Professional Aspirations", "Профессиональные цели"));
        }
    }

    private void addPersonalExpected(List<String> expected, ResumeRenderData data) {
        boolean hasPersonalLine = hasText(data.getPersonalLine1())
                || hasText(data.getPersonalLine2())
                || hasText(data.getPersonalLine3());
        if (!hasPersonalLine) return;

        addIfPresent(expected, sectionTitle(data, "Personal Information", "Персональная информация"));
        addStableAnchor(expected, data.getPersonalLine1(), PERSONAL_ANCHOR_TOKENS);
        addStableAnchor(expected, data.getPersonalLine2(), PERSONAL_ANCHOR_TOKENS);
        addStableAnchor(expected, data.getPersonalLine3(), PERSONAL_ANCHOR_TOKENS);
    }

    private void addIfPresent(List<String> expected, String value) {
        if (hasText(value)) expected.add(value);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private List<String> uniqueNonBlank(List<String> expected) {
        return expected.stream().filter(this::hasText).distinct().toList();
    }

    private String sectionTitle(ResumeRenderData data, String en, String ru) {
        return "RU".equalsIgnoreCase(data.getLanguageCode()) ? ru : en;
    }
}
