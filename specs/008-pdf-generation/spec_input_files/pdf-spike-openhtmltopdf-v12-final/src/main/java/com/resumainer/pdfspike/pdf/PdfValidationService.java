package com.resumainer.pdfspike.pdf;

import com.resumainer.pdfspike.model.FillTarget;
import com.resumainer.pdfspike.model.PdfMetrics;

import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.text.Normalizer;

public final class PdfValidationService {
    public String validate(PdfMetrics metrics, int expectedPageCount, Map<Integer, FillTarget> targets, List<String> expectedTexts) {
        if (!metrics.selectableText()) return "PDF_TEXT_NOT_SELECTABLE";
        if (metrics.actualPageCount() != expectedPageCount) {
            boolean onlyTrailingBlankPages = metrics.actualPageCount() > expectedPageCount;
            for (int page = expectedPageCount + 1; page <= metrics.actualPageCount(); page++) {
                if (metrics.fillRatios().getOrDefault(page, 0.0) > 0.001) {
                    onlyTrailingBlankPages = false;
                    break;
                }
            }
            if (onlyTrailingBlankPages) return "TRAILING_BLANK_PAGE expected=" + expectedPageCount + " actual=" + metrics.actualPageCount();
            return "PAGE_COUNT_MISMATCH expected=" + expectedPageCount + " actual=" + metrics.actualPageCount();
        }
        List<String> missingTexts = missingTexts(metrics.extractedText(), expectedTexts);
        if (!missingTexts.isEmpty()) return "MISSING_TEXTS " + missingTexts;
        for (FillTarget t : targets.values()) {
            double fill = metrics.fillRatios().getOrDefault(t.pageNumber(), 0.0);
            if (t.requiredNonEmpty() && fill <= 0.001) return "PAGE_EMPTY page=" + t.pageNumber();
            if (fill < t.minFillRatio()) return "UNDERFILLED page=" + t.pageNumber() + " fill=" + fill + " min=" + t.minFillRatio();
            if (t.maxFillRatio() != null && fill > t.maxFillRatio()) return "OVERFILLED page=" + t.pageNumber() + " fill=" + fill + " max=" + t.maxFillRatio();
        }
        return "OK";
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

    private String normalize(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFKC)
                .replace('­', ' ')
                .replace('﻿', ' ')
                .replace('￾', ' ')
                .replace('Ё', 'Е')
                .replace('ё', 'е')
                .replaceAll("[\u2010-\u2015\u2212]", "-");

        // PDF extraction may drop a hyphen between letters at a line break, especially in RU text.
        // Compare letter-hyphen-letter as the same word to avoid false MISSING_TEXTS.
        normalized = normalized.replaceAll("(?iu)(?<=\\p{L})-(?=\\p{L})", "");

        return normalized.toUpperCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
    }
}
