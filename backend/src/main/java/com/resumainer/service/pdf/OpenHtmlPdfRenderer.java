package com.resumainer.service.pdf;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * Renders HTML/XHTML to PDF using OpenHTMLToPDF + PDFBox.
 * Uses fast mode and programmatic font loading from a resources directory.
 * Ported from spike V12.1. Adapted: Path → File.
 */
public final class OpenHtmlPdfRenderer {
    private static final Logger log = LoggerFactory.getLogger(OpenHtmlPdfRenderer.class);

    private final File resourcesDir;

    public OpenHtmlPdfRenderer(File resourcesDir) {
        this.resourcesDir = resourcesDir;
    }

    public void render(String html, File pdfFile) {
        try {
            Files.createDirectories(pdfFile.getParentFile().toPath());
            try (OutputStream os = new FileOutputStream(pdfFile)) {
                PdfRendererBuilder builder = new PdfRendererBuilder();
                builder.useFastMode();
                builder.withHtmlContent(html, resourcesDir.toURI().toString());
                builder.toStream(os);
                builder.run();
            }
            log.debug("PDF rendered: {}", pdfFile);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to render PDF: " + pdfFile, e);
        }
    }
}
