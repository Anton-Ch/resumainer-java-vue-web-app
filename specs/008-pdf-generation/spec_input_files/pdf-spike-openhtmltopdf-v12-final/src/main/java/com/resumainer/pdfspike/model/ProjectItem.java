package com.resumainer.pdfspike.model;

import java.util.List;

public record ProjectItem(
        String title,
        String role,
        String location,
        String startDate,
        String endDate,
        String description,
        List<String> bullets
) {}
