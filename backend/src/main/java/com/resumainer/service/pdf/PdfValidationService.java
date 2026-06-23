package com.resumainer.service.pdf;

import com.resumainer.model.PdfFillTarget;
import com.resumainer.model.pdf.PdfMetrics;

import java.text.Normalizer;
import java.util.*;

/**
 * Validates a generated PDF against expected page count, fill targets, and content.
 * Ported from spike V12.1. Adapted to use production PdfFillTarget and PdfMetrics.
 */
public final class PdfValidationService {

    public String validate(PdfMetrics metrics, int expectedPageCount,
                           List<PdfFillTarget> targets, List<String> expectedTexts) {
        if (!metrics.selectableText()) return "PDF_TEXT_NOT_SELECTABLE";
        if (metrics.actualPageCount() != expectedPageCount) {
            boolean onlyTrailingBlankPages = metrics.actualPageCount() > expectedPageCount;
            for (int page = expectedPageCount + 1; page <= metrics.actualPageCount(); page++) {
                if (metrics.fillRatios().getOrDefault(page, 0.0) > 0.001) {
                    onlyTrailingBlankPages = false;
                    break;
                }
            }
            if (onlyTrailingBlankPages)
                return "TRAILING_BLANK_PAGE expected=" + expectedPageCount + " actual=" + metrics.actualPageCount();
            return "PAGE_COUNT_MISMATCH expected=" + expectedPageCount + " actual=" + metrics.actualPageCount();
        }
        List<String> missingTexts = missingTexts(metrics.extractedText(), expectedTexts);
        if (!missingTexts.isEmpty()) return "MISSING_TEXTS " + missingTexts;

        String footerProblem = footerSafeZoneProblem(metrics);
        if (footerProblem != null) return footerProblem;

        if (targets != null) {
            for (PdfFillTarget t : targets) {
            double fill = metrics.fillRatios().getOrDefault(t.getPageNumber(), 0.0);
            if (fill <= 0.001) return "PAGE_EMPTY page=" + t.getPageNumber();
            if (fill < t.getMinFill().doubleValue())
                return "UNDERFILLED page=" + t.getPageNumber() + " fill=" + fill + " min=" + t.getMinFill();
            if (t.getMaxFill() != null && fill > t.getMaxFill().doubleValue())
                return "OVERFILLED page=" + t.getPageNumber() + " fill=" + fill + " max=" + t.getMaxFill();
            }
        }
        return "OK";
    }

    private String footerSafeZoneProblem(PdfMetrics metrics) {
        if (metrics.bottomNotePresentByPage() == null || metrics.bottomNotePresentByPage().isEmpty()) {
            return null;
        }

        for (Map.Entry<Integer, Boolean> entry : metrics.bottomNotePresentByPage().entrySet()) {
            int page = entry.getKey();
            boolean bottomNotePresent = Boolean.TRUE.equals(entry.getValue());
            if (!bottomNotePresent) continue;

            int bottomSafeZoneLines = metrics.bottomSafeZoneTextLineCounts() != null
                    ? metrics.bottomSafeZoneTextLineCounts().getOrDefault(page, 0)
                    : 0;

            // One line is expected: the footer note itself.
            // More than one line means normal content has entered the footer safe zone.
            if (bottomSafeZoneLines > 1) {
                return "FOOTER_OVERLAP page=" + page + " bottomSafeZoneLines=" + bottomSafeZoneLines;
            }
        }
        return null;
    }

    private List<String> missingTexts(String extractedText, List<String> expectedTexts) {
        String haystack = normalize(extractedText);
        List<String> missing = new ArrayList<>();
        for (String expected : expectedTexts) {
            if (!haystack.contains(normalize(expected))) missing.add(expected);
            if (missing.size() >= 8) break;
        }
        return missing;
    }

    static String normalize(String value) {
        if (value == null) return "";

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
                .replace('\u00ad', ' ')   // soft hyphen
                .replace('\ufeff', ' ')   // BOM
                .replace('\ufffe', ' ')   // byte order mark variant
                .replace('Ё', 'Е')
                .replace('ё', 'е')
                .replaceAll("[\u2010-\u2015\u2212]", "-");

        // PDF extraction often changes punctuation, list markers, pipes, slashes,
        // commas, periods, and bullets. Validate semantic token order instead.
        normalized = normalized.replaceAll("[^\\p{L}\\p{N}]+", " ");

        return normalized.toUpperCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }
}
