package com.resumainer.service.pdf;

import com.resumainer.model.PdfFillTarget;
import com.resumainer.model.pdf.PdfMetrics;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class PdfValidationServiceTest {

    private final PdfValidationService validator = new PdfValidationService();

    @Test
    void validate_okForCorrectPdf() {
        PdfMetrics metrics = new PdfMetrics(1, true, Map.of(1, 0.85), "Hello World");
        PdfFillTarget target = new PdfFillTarget();
        target.setPageNumber(1);
        target.setMinFill(new java.math.BigDecimal("0.50"));
        target.setMaxFill(new java.math.BigDecimal("0.96"));

        String result = validator.validate(metrics, 1, List.of(target), List.of("Hello World"));
        assertEquals("OK", result);
    }

    @Test
    void validate_rejectsNonSelectableText() {
        PdfMetrics metrics = new PdfMetrics(1, false, Map.of(), "");
        String result = validator.validate(metrics, 1, List.of(), List.of());
        assertTrue(result.contains("PDF_TEXT_NOT_SELECTABLE"));
    }

    @Test
    void validate_rejectsPageCountMismatch() {
        PdfMetrics metrics = new PdfMetrics(3, true, Map.of(1, 0.5, 2, 0.5, 3, 0.5), "text");
        String result = validator.validate(metrics, 2, List.of(), List.of());
        assertTrue(result.contains("PAGE_COUNT_MISMATCH"));
    }

    @Test
    void validate_rejectsMissingText() {
        PdfMetrics metrics = new PdfMetrics(1, true, Map.of(1, 0.85), "only this text");
        String result = validator.validate(metrics, 1, List.of(), List.of("missing text"));
        assertTrue(result.contains("MISSING_TEXTS"));
    }

    @Test
    void normalize_handlesCyrillicHyphens() {
        String result = PdfValidationService.normalize("бизнес-правила");
        // After normalization, hyphen between letters is removed
        assertFalse(result.contains("-"), "Hyphen between letters should be removed: " + result);
    }
}
