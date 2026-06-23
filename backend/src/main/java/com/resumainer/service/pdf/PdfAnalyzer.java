package com.resumainer.service.pdf;

import com.resumainer.model.pdf.PdfMetrics;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Analyzes a generated PDF: page count, text extraction, fill ratios,
 * and footer safe-zone text rows.
 *
 * Footer-safe-zone logic allows PdfValidationService to catch visual overlap
 * between normal page content and the bottom navigation note.
 */
public final class PdfAnalyzer {

    /** Bottom area reserved for "See the next page" note on pages with has-next. */
    static final float FOOTER_SAFE_ZONE_MM = 18.0f;
    private static final float POINTS_PER_MM = 72.0f / 25.4f;
    private static final float ROW_BUCKET_PT = 3.0f;

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

            return new PdfMetrics(
                    document.getNumberOfPages(),
                    text != null && !text.isBlank(),
                    fill,
                    text == null ? "" : text,
                    stripper.bottomSafeZoneTextLineCounts(),
                    stripper.bottomNotePresentByPage());
        } catch (IOException e) {
            throw new IllegalStateException("Failed to analyze PDF: " + pdfFile.getPath(), e);
        }
    }

    private static final class PositionStripper extends PDFTextStripper {
        private final Map<Integer, List<Float>> ysByPage = new HashMap<>();
        private final Map<Integer, Map<Integer, StringBuilder>> bottomRowsByPage = new HashMap<>();

        private PositionStripper() throws IOException {
            super();
            setSortByPosition(true);
        }

        @Override
        protected void processTextPosition(TextPosition text) {
            String s = text.getUnicode();
            if (s != null && !s.isBlank()) {
                int page = getCurrentPageNo();
                ysByPage.computeIfAbsent(page, k -> new ArrayList<>()).add(text.getYDirAdj());

                if (isInsideBottomSafeZone(text)) {
                    int rowKey = bottomRowKey(text.getYDirAdj());
                    bottomRowsByPage
                            .computeIfAbsent(page, k -> new TreeMap<>())
                            .computeIfAbsent(rowKey, k -> new StringBuilder())
                            .append(s);
                }
            }
            super.processTextPosition(text);
        }

        private boolean isInsideBottomSafeZone(TextPosition text) {
            float pageHeight = text.getPageHeight();
            float zoneStartY = pageHeight - (FOOTER_SAFE_ZONE_MM * POINTS_PER_MM);
            return text.getYDirAdj() >= zoneStartY;
        }

        private int bottomRowKey(float y) {
            return Math.round(y / ROW_BUCKET_PT);
        }

        private Map<Integer, Integer> bottomSafeZoneTextLineCounts() {
            Map<Integer, Integer> result = new LinkedHashMap<>();
            for (Map.Entry<Integer, Map<Integer, StringBuilder>> page : bottomRowsByPage.entrySet()) {
                int count = 0;
                for (StringBuilder row : page.getValue().values()) {
                    if (row != null && !row.toString().isBlank()) count++;
                }
                result.put(page.getKey(), count);
            }
            return result;
        }

        private Map<Integer, Boolean> bottomNotePresentByPage() {
            Map<Integer, Boolean> result = new LinkedHashMap<>();
            for (Map.Entry<Integer, Map<Integer, StringBuilder>> page : bottomRowsByPage.entrySet()) {
                boolean found = page.getValue().values().stream()
                        .map(StringBuilder::toString)
                        .anyMatch(PositionStripper::isBottomNoteText);
                result.put(page.getKey(), found);
            }
            return result;
        }

        private static boolean isBottomNoteText(String value) {
            String normalized = value == null ? "" : value
                    .replace('Ё', 'Е')
                    .replace('ё', 'е')
                    .toUpperCase(Locale.ROOT)
                    .replaceAll("[^\\p{L}\\p{N}]+", " ")
                    .replaceAll("\\s+", " ")
                    .trim();

            return normalized.contains("SEE THE NEXT PAGE")
                    || normalized.contains("NEXT PAGE")
                    || normalized.contains("СМ СЛЕДУЮЩУЮ СТРАНИЦУ")
                    || normalized.contains("СЛЕДУЮЩУЮ СТРАНИЦУ");
        }
    }
}
