package com.resumainer.service.pdf;

import com.resumainer.model.pdf.PdfMetrics;

import java.io.File;

/**
 * Detects and removes trailing blank pages from a PDF.
 * Ported from spike V12.1. Adapted: Path → File.
 */
public final class PdfBlankPageCleaner {
    private static final double BLANK_FILL_THRESHOLD = 0.001;
    private final PdfAnalyzer analyzer;

    public PdfBlankPageCleaner(PdfAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    public int removeTrailingBlankPages(File pdfFile) {
        PdfMetrics metrics = analyzer.analyze(pdfFile);
        int removeCount = 0;
        for (int page = metrics.actualPageCount(); page >= 2; page--) {
            double fill = metrics.fillRatios().getOrDefault(page, 0.0);
            if (fill <= BLANK_FILL_THRESHOLD) removeCount++;
            else break;
        }
        if (removeCount == 0) return 0;
        try (org.apache.pdfbox.pdmodel.PDDocument document =
                     org.apache.pdfbox.pdmodel.PDDocument.load(pdfFile)) {
            for (int i = 0; i < removeCount; i++) {
                document.removePage(document.getNumberOfPages() - 1);
            }
            document.save(pdfFile);
            return removeCount;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to remove trailing blank pages from " + pdfFile, e);
        }
    }
}
