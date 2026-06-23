package com.resumainer.model.pdf;

import java.util.Map;

/**
 * PDF generation metrics for a single fitting attempt.
 * Ported from spike V12.1 PdfMetrics record.
 *
 * bottomSafeZoneTextLineCounts / bottomNotePresentByPage are used to detect
 * visual footer overlap: footer note itself is allowed, but normal content
 * must not enter the footer safe zone.
 */
public record PdfMetrics(
        int actualPageCount,
        boolean selectableText,
        Map<Integer, Double> fillRatios,
        String extractedText,
        Map<Integer, Integer> bottomSafeZoneTextLineCounts,
        Map<Integer, Boolean> bottomNotePresentByPage
) {
    /** Backward-compatible constructor for existing tests and callers. */
    public PdfMetrics(int actualPageCount, boolean selectableText,
                      Map<Integer, Double> fillRatios, String extractedText) {
        this(actualPageCount, selectableText, fillRatios, extractedText, Map.of(), Map.of());
    }
}
