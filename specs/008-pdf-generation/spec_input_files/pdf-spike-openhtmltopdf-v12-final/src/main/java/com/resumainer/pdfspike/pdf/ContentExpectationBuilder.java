package com.resumainer.pdfspike.pdf;

import com.resumainer.pdfspike.model.*;

import java.util.ArrayList;
import java.util.List;

public final class ContentExpectationBuilder {
    public List<String> build(ResumeData data, PagePlan plan) {
        List<String> expected = new ArrayList<>();
        addHeaderExpected(expected, data);
        addIfPresent(expected, title(data, "Professional Summary", "О себе"));
        addIfPresent(expected, title(data, "Work Experience", "Опыт работы"));
        addIfPresent(expected, title(data, "Skills", "Навыки"));
        addIfPresent(expected, title(data, "Education", "Образование"));
        addAspirationsExpected(expected, data);
        addPersonalExpected(expected, data);
        for (WorkExperience w : plan.page1Work()) addIfPresent(expected, w.role());
        for (WorkExperience w : plan.page2AdditionalWork()) addIfPresent(expected, w.role());
        for (ProjectItem p : plan.page2Projects()) addIfPresent(expected, p.title());
        for (CourseItem c : data.courses()) addIfPresent(expected, c.name());
        return uniqueNonBlank(expected);
    }

    public List<String> buildForPlannedPage(ResumeData data, PagePlan plan, int plannedPageNumber) {
        List<String> expected = new ArrayList<>();
        if (plannedPageNumber == 1) {
            addHeaderExpected(expected, data);
            addIfPresent(expected, title(data, "Professional Summary", "О себе"));
            addIfPresent(expected, title(data, "Work Experience", "Опыт работы"));
            addIfPresent(expected, title(data, "Skills", "Навыки"));
            addIfPresent(expected, title(data, "Education", "Образование"));
            for (WorkExperience w : plan.page1Work()) addIfPresent(expected, w.role());
            for (CourseItem c : data.courses()) addIfPresent(expected, c.name());

            // A one-page resume renders aspirations and personal information on planned page 1.
            // These fields are optional in the real profile schema, so require only non-blank rendered lines.
            if (plan.targetPageCount() == 1) {
                addAspirationsExpected(expected, data);
                addPersonalExpected(expected, data);
            }
        } else if (plannedPageNumber == 2) {
            for (ProjectItem p : plan.page2Projects()) addIfPresent(expected, p.title());
            for (WorkExperience w : plan.page2AdditionalWork()) addIfPresent(expected, w.role());
            if (!plan.page2Projects().isEmpty()) addIfPresent(expected, title(data, "Projects and Volunteering", "Проекты и волонтёрство"));
            if (!plan.page2AdditionalWork().isEmpty()) addIfPresent(expected, title(data, "Additional Work Experience", "Дополнительный опыт работы"));
            if (plan.targetPageCount() <= 2) {
                addAspirationsExpected(expected, data);
                addPersonalExpected(expected, data);
            }
        } else if (plannedPageNumber == 3) {
            addAspirationsExpected(expected, data);
            addPersonalExpected(expected, data);
        }
        return uniqueNonBlank(expected);
    }

    private void addHeaderExpected(List<String> expected, ResumeData data) {
        addIfPresent(expected, data.fullName());
        addIfPresent(expected, data.title());
        addIfPresent(expected, data.email());
        addIfPresent(expected, data.valueLine());
    }

    private void addAspirationsExpected(List<String> expected, ResumeData data) {
        if (hasText(data.aspirations())) {
            addIfPresent(expected, title(data, "Professional Aspirations", "Профессиональные цели"));
            addLongTextAnchors(expected, data.aspirations());
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
                .replace('­', ' ')
                .replace('﻿', ' ')
                .replace('￾', ' ')
                .replaceAll("[\u2010-\u2015\u2212]", "-")
                                .replaceAll("[^\\p{L}\\p{N}\\s\\-]", " ")
                .replaceAll("\\s+", " ")
                .trim();
        if (cleaned.isEmpty()) return List.of();
        return List.of(cleaned.split(" "));
    }

    private void addPersonalExpected(List<String> expected, ResumeData data) {
        boolean hasPersonalLine = hasText(data.personalLine1()) || hasText(data.personalLine2()) || hasText(data.personalLine3());
        if (!hasPersonalLine) return;
        addIfPresent(expected, title(data, "Personal Information", "Персональная информация"));
        addIfPresent(expected, data.personalLine1());
        addIfPresent(expected, data.personalLine2());
        addIfPresent(expected, data.personalLine3());
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

    private String title(ResumeData d, String en, String ru) {
        return d.language() == Language.RU ? ru : en;
    }
}
