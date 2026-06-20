package com.resumainer.pdfspike.model;

public record EdgeCaseRule(
        int ecNumber,
        int minWork,
        int maxWork,
        int projectCount,
        int courseCount,
        TemplateMode templateMode,
        int page1WorkItems,
        int page2WorkItems,
        int maxTotalWorkItems,
        String reason
) {
    public int expectedPages() {
        return templateMode == TemplateMode.ONE_PAGE ? 1 : 2;
    }
}
