package com.resumainer.service.pdf;

import com.resumainer.model.pdf.PdfMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for OpenHtmlPdfRenderer and PdfAnalyzer.
 * Verifies HTML→PDF rendering produces valid PDF output with selectable text.
 */
class OpenHtmlPdfRendererTest {

    @TempDir
    Path tempDir;

    @Test
    void render_producesValidPdfWithSelectableText() throws Exception {
        File resourcesDir = tempDir.toFile();
        OpenHtmlPdfRenderer renderer = new OpenHtmlPdfRenderer(resourcesDir);

        String html = "<html><body><h1>Test PDF</h1><p>Hello World</p></body></html>";
        File pdfFile = new File(tempDir.toFile(), "test.pdf");
        renderer.render(html, pdfFile);

        assertTrue(pdfFile.exists(), "PDF file must exist");
        assertTrue(pdfFile.length() > 0, "PDF file must be non-empty");

        // Verify PDF header signature
        byte[] header = java.nio.file.Files.readAllBytes(pdfFile.toPath());
        String headerStr = new String(header, 0, 5);
        assertTrue(headerStr.startsWith("%PDF"), "PDF must start with %PDF signature: " + headerStr);

        // Analyze with PdfAnalyzer
        PdfAnalyzer analyzer = new PdfAnalyzer();
        PdfMetrics metrics = analyzer.analyze(pdfFile);

        assertEquals(1, metrics.actualPageCount(), "Simple HTML should produce 1 page");
        assertTrue(metrics.selectableText(), "PDF must contain selectable text");
        assertTrue(metrics.extractedText().contains("Hello World"), "Extracted text must contain expected content");
    }
}
