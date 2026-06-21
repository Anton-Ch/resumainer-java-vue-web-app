package com.resumainer.service.pdf;

import com.resumainer.model.pdf.ResumeRenderData;
import com.resumainer.model.pdf.PagePlan;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds expected content strings for PDF validation.
 * Ported from spike V12.1. Adapted to use production ResumeRenderData and PagePlan.
 */
public final class ContentExpectationBuilder {

    /** Build expected text anchors for the full resume across all pages. */
    public List<String> build(ResumeRenderData data, PagePlan plan) {
        List<String> expected = new ArrayList<>();
        addHeaderExpected(expected, data);
        addIfPresent(expected, sectionTitle(data, "Professional Summary", "О себе"));
        addIfPresent(expected, sectionTitle(data, "Work Experience", "Опыт работы"));
        addIfPresent(expected, sectionTitle(data, "Skills", "Навыки"));
        addIfPresent(expected, sectionTitle(data, "Education", "Образование"));
        addAspirationsExpected(expected, data);
        addPersonalExpected(expected, data);
        for (ResumeRenderData.RenderWorkItem w : data.getWorkExperience()) {
            addIfPresent(expected, w.getJobTitle());
        }
        for (ResumeRenderData.RenderProjectItem p : data.getProjects()) {
            addIfPresent(expected, p.getProjectName());
        }
        for (ResumeRenderData.RenderCourseItem c : data.getCourses()) {
            addIfPresent(expected, c.getName());
        }
        return uniqueNonBlank(expected);
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
            addLongTextAnchors(expected, data.getProfessionalAspirations());
        }
    }

    private void addLongTextAnchors(List<String> expected, String text) {
        if (!hasText(text)) return;
        List<String> tokens = anchorTokens(text);
        if (tokens.size() <= 16) {
            addIfPresent(expected, text);
            return;
        }
        int anchorSize = Math.min(8, tokens.size());
        addIfPresent(expected, String.join(" ", tokens.subList(0, anchorSize)));
        addIfPresent(expected, String.join(" ", tokens.subList(tokens.size() - anchorSize, tokens.size())));
    }

    private List<String> anchorTokens(String text) {
        String cleaned = text
                .replace('\u00ad', ' ')
                .replace('\ufeff', ' ')
                .replace('\ufffe', ' ')
                .replaceAll("[\u2010-\u2015\u2212]", "-")
                .replaceAll("[^\\p{L}\\p{N}\\s\\-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.isEmpty()) return List.of();
        return List.of(cleaned.split(" "));
    }

    private void addPersonalExpected(List<String> expected, ResumeRenderData data) {
        boolean hasPersonalLine = hasText(data.getPersonalLine1())
                || hasText(data.getPersonalLine2())
                || hasText(data.getPersonalLine3());
        if (!hasPersonalLine) return;
        addIfPresent(expected, sectionTitle(data, "Personal Information", "Персональная информация"));
        addIfPresent(expected, data.getPersonalLine1());
        addIfPresent(expected, data.getPersonalLine2());
        addIfPresent(expected, data.getPersonalLine3());
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
