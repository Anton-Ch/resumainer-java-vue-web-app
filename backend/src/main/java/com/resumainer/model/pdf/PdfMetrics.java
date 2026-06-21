package com.resumainer.model.pdf;

import java.util.Map;

/**
 * PDF generation metrics for a single fitting attempt.
 * Ported from spike V12.1 PdfMetrics record.
 */
public record PdfMetrics(
        int actualPageCount,
        boolean selectableText,
        Map<Integer, Double> fillRatios,
        String extractedText
) {}
