package com.resumainer.service.pdf;

import com.resumainer.model.pdf.PdfMetrics;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Analyzes a generated PDF: page count, text extraction, fill ratios.
 * Ported from spike V12.1. Adapted: Path → File for production compatibility.
 */
public final class PdfAnalyzer {

    public PdfMetrics analyze(File pdfFile) {
        try (PDDocument document = PDDocument.load(pdfFile)) {
            PositionStripper stripper = new PositionStripper();
            String text = stripper.getText(document);
            Map<Integer, Double> fill = new LinkedHashMap<>();
            for (int page = 1; page <= document.getNumberOfPages(); page++) {
                List<Float> ys = stripper.ysByPage.getOrDefault(page, List.of());
                if (ys.isEmpty()) {
                    fill.put(page, 0.0);
                } else {
                    float min = Collections.min(ys);
                    float max = Collections.max(ys);
                    float pageHeight = document.getPage(page - 1).getMediaBox().getHeight();
                    fill.put(page, Math.max(0.0, Math.min(1.20, (max - min) / pageHeight)));
                }
            }
            return new PdfMetrics(document.getNumberOfPages(), text != null && !text.isBlank(), fill, text == null ? "" : text);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to analyze PDF: " + pdfFile.getPath(), e);
        }
    }

    private static final class PositionStripper extends PDFTextStripper {
        private final Map<Integer, List<Float>> ysByPage = new HashMap<>();
        private PositionStripper() throws IOException { super(); setSortByPosition(true); }

        @Override
        protected void processTextPosition(TextPosition text) {
            String s = text.getUnicode();
            if (s != null && !s.isBlank()) {
                ysByPage.computeIfAbsent(getCurrentPageNo(), k -> new ArrayList<>()).add(text.getYDirAdj());
            }
            super.processTextPosition(text);
        }
    }
}
