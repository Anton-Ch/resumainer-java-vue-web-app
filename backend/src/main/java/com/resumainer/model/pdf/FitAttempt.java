package com.resumainer.model.pdf;

/**
 * Record of a single fitting attempt with its parameters and validation result.
 * Ported from spike V12.1 FitAttempt record.
 */
public record FitAttempt(
        int attemptNumber,
        int targetPageCount,
        FitState state,
        PdfMetrics metrics,
        boolean valid,
        String reason,
        String htmlPath,
        String pdfPath
) {}
