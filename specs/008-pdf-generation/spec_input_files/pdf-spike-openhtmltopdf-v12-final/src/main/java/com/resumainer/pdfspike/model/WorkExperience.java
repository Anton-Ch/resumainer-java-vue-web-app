package com.resumainer.pdfspike.model;

import java.util.List;

public record WorkExperience(
        String role,
        String company,
        String location,
        String startDate,
        String endDate,
        String description,
        List<String> bullets
) {}
