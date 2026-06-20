package com.resumainer.pdfspike.model;

import java.util.List;

public record ResumeData(
        Language language,
        int ecNumber,
        String fullName,
        String title,
        String phone,
        String email,
        String location,
        String linkedin,
        String portfolio,
        String telegram,
        String whatsapp,
        String valueLine,
        String summary,
        List<WorkExperience> workExperience,
        List<SkillGroup> skills,
        List<String> education,
        List<CourseItem> courses,
        List<ProjectItem> projects,
        String aspirations,
        String personalLine1,
        String personalLine2,
        String personalLine3
) {}
