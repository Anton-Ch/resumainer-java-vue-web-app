package com.resumainer.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for NoOpPdfGenerationService.
 * Verifies convertPdf returns false (not yet implemented).
 */
class NoOpPdfGenerationServiceTest {

    private final NoOpPdfGenerationService service = new NoOpPdfGenerationService();

    @Test
    void convertPdf_returnsFalse() {
        assertFalse(service.convertPdf("/path/to/file.html", "/path/to/file.pdf"));
    }
}
