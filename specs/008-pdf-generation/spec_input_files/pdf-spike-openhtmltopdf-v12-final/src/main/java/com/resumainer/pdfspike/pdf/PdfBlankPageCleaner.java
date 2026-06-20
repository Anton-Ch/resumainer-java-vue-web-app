package com.resumainer.pdfspike.pdf;

import com.resumainer.pdfspike.model.PdfMetrics;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.nio.file.Path;

public final class PdfBlankPageCleaner {
    private static final double BLANK_FILL_THRESHOLD = 0.001;
    private final PdfAnalyzer analyzer;

    public PdfBlankPageCleaner(PdfAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    public int removeTrailingBlankPages(Path pdfPath) {
        PdfMetrics metrics = analyzer.analyze(pdfPath);
        int removeCount = 0;
        for (int page = metrics.actualPageCount(); page >= 2; page--) {
            double fill = metrics.fillRatios().getOrDefault(page, 0.0);
            if (fill <= BLANK_FILL_THRESHOLD) removeCount++;
            else break;
        }
        if (removeCount == 0) return 0;
        try (PDDocument document = PDDocument.load(pdfPath.toFile())) {
            for (int i = 0; i < removeCount; i++) {
                document.removePage(document.getNumberOfPages() - 1);
            }
            document.save(pdfPath.toFile());
            return removeCount;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to remove trailing blank pages from " + pdfPath, e);
        }
    }
}
