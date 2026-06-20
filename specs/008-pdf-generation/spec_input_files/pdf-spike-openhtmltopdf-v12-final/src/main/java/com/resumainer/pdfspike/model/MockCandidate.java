package com.resumainer.pdfspike.model;

public record MockCandidate(
        int ecNumber,
        String enFullName,
        String ruFullName,
        String enTitle,
        String ruTitle,
        String phone,
        String email,
        String enLocation,
        String ruLocation,
        String linkedin,
        String portfolio,
        String telegram,
        String whatsapp,
        int workCount,
        int projectCount,
        int courseCount
) {}
