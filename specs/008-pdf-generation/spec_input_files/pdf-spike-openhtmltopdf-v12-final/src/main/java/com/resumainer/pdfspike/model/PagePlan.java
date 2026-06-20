package com.resumainer.pdfspike.model;

import java.util.List;

public record PagePlan(
        EdgeCaseRule rule,
        int targetPageCount,
        List<WorkExperience> page1Work,
        List<WorkExperience> page2AdditionalWork,
        List<ProjectItem> page2Projects,
        boolean page2HasProjectsFirst
) {}
